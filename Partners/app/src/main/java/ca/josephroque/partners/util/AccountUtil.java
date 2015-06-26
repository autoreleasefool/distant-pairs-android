package ca.josephroque.partners.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ca.josephroque.partners.R;
import ca.josephroque.partners.database.DBHelper;

/**
 * Created by Joseph Roque on 2015-06-16.
 *
 * Classes and methods for creating and managing an account.
 */
@SuppressWarnings({"Convert2Lambda", "Convert2streamapi"})
public final class AccountUtil
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "AccountUtil";

    /** Represents password in preferences. */
    public static final String PASSWORD = "account_password";
    /** Represents account name in preferences. */
    public static final String USERNAME = "account_username";
    /** Represents pair's name in preferences. */
    public static final String PAIR = "account_pair";
    /** Represents parse object containing the user's registered pair. */
    public static final String PARSE_PAIR_ID = "account_pair_objid";
    /** Represents parse object container the user's account. */
    public static final String PARSE_USER_ID = "account_user_objid";

    /** Number of random bits to generate. */
    private static final int PASSWORD_BIT_LENGTH = 130;
    /** Maximum character length for usernames. */
    public static final int USERNAME_MAX_LENGTH = 16;
    /** Represents successful account related operation. */
    public static final int SUCCESS = 0;

    /** Represents the minimum length of a valid account deletion key. */
    private static final int ACCOUNT_DELETION_KEY_LENGTH = 35;

    /** Number base. */
    private static final byte BASE = 32;

    /** Secure random number generator. */
    private static SecureRandom sSecureRandom = new SecureRandom();

    /**
     * Default private constructor.
     */
    private AccountUtil()
    {
        // does nothing
    }

    /**
     * Checks if username is valid and, if it is, removes uppercase letters.
     *
     * @param username username to validate
     * @return {@code username} without uppercase letter if it contains only letters and numbers, or
     * null if the username is otherwise invalid.
     */
    public static String validateUsername(String username)
    {
        if (!username.matches("^[a-zA-Z0-9]+$") || username.length() > USERNAME_MAX_LENGTH)
            return null;

        return username.toLowerCase();
    }

    /**
     * Generates a random 130 bit password.
     *
     * @return random password
     */
    public static String randomAlphaNumericPassword()
    {
        return new BigInteger(PASSWORD_BIT_LENGTH, sSecureRandom).toString(BASE);
    }

    /**
     * Prompt user to delete their account.
     *
     * @param context to create dialog
     * @param callback to inform calling method if account is deleted
     */
    public static void promptDeleteAccount(final Context context,
                                           final DeleteAccountCallback callback)
    {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    callback.onDeleteAccountStarted();
                    deleteAccount(context, callback);

                }
            }
        };

        new AlertDialog.Builder(context)
                .setTitle("Delete account?")
                .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                .setNegativeButton(R.string.text_dialog_cancel, listener)
                .setPositiveButton(R.string.text_dialog_okay, listener)
                .create()
                .show();
    }

    /**
     * Deletes the current user account.
     *
     * @param context to get shared preferences
     * @param callback to inform calling method if account is deleted
     */
    public static void deleteAccount(final Context context, final DeleteAccountCallback callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String deletionKey = null;
                HashMap<String, String> deletionMap = new HashMap<>();

                DBHelper.clearAllThoughts(context);

                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null)
                {
                    try
                    {
                        deletionKey = ParseCloud.callFunction("requestAccountDeletionKey",
                                deletionMap);
                        if (deletionKey == null
                                || deletionKey.length() < ACCOUNT_DELETION_KEY_LENGTH)
                            throw new ParseException(ParseException.OTHER_CAUSE, "no key");
                        ParseUser.logOut();
                    }
                    catch (ParseException ex)
                    {
                        if (callback != null)
                            callback.onDeleteAccountError(null);
                        ParseUser.logOut();
                        return;
                    }
                }

                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(context);

                String username = preferences.getString(USERNAME, null);
                if (username == null)
                    return;

                preferences.edit()
                        .remove(USERNAME)
                        .remove(PASSWORD)
                        .remove(PAIR)
                        .remove(PARSE_PAIR_ID)
                        .remove(MessageUtil.STATUS_OBJECT_ID)
                        .apply();

                deleteAccountObjects(username);

                if (deletionKey != null)
                {
                    deletionMap.put("key", deletionKey);
                    deletionMap.put("username", username);
                    deleteAccountCloudCode(deletionMap, callback);
                }

                if (callback != null)
                    callback.onDeleteAccountEnded();
            }
        }).start();
    }

    /**
     * Deletes all Parse objects associated with the username.
     *
     * @param username user to delete objects for
     */
    private static void deleteAccountObjects(String username)
    {
        ParseQuery<ParseObject> pairQuery = ParseQuery.or(Arrays.asList(
                new ParseQuery<>("Pair").whereEqualTo(USERNAME, username),
                new ParseQuery<>("Pair").whereEqualTo(PAIR, username)));

        ParseQuery<ParseObject> statusQuery = ParseQuery.getQuery("Status")
                .whereEqualTo(AccountUtil.USERNAME, username);

        ParseQuery<ParseObject> thoughtQuery = ParseQuery.or(Arrays.asList(
                new ParseQuery<>("Thought").whereEqualTo("recipientName", username),
                new ParseQuery<>("Thought").whereEqualTo("senderName", username)));

        deleteObjectsFromQuery(pairQuery);
        deleteObjectsFromQuery(statusQuery);
        deleteObjectsFromQuery(thoughtQuery);
    }

    /**
     * Calls "deleteAccount" method in Parse cloud code.
     *
     * @param map hash map with username and deletion key
     * @param callback to inform calling method if account is deleted
     */
    private static void deleteAccountCloudCode(HashMap<String, String> map,
                                               final DeleteAccountCallback callback)
    {
        ParseCloud.callFunctionInBackground("deleteAccount", map,
                new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, ParseException e) {
                        if (e == null)
                            callback.onDeleteAccountEnded();
                        else
                            callback.onDeleteAccountError(null);
                    }
                });
    }

    /**
     * Deletes objects from Parse database which are returned by the query.
     *
     * @param query parse database query
     */
    private static void deleteObjectsFromQuery(ParseQuery<ParseObject> query)
    {
        try
        {
            List<ParseObject> results = query.find();
            for (ParseObject res : results)
                res.deleteEventually();
        }
        catch (ParseException ex)
        {
            // does nothing - user can assume these objects were deleted
        }
    }

    /**
     * Checks if a partner has been registered in the application.
     *
     * @param context to get shared preferences
     * @return true if a partner exists in shared preferences
     */
    public static boolean doesPartnerExist(Context context)
    {
        String partnerName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PAIR, null);
        return partnerName != null && partnerName.length() > 0;
    }

    /**
     * Checks if a user has been registered in the application.
     *
     * @param context to get shared preferences
     * @return true if a username and password exists in shared preferences
     */
    public static boolean doesAccountExist(Context context)
    {
        String username = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USERNAME, null);
        String password = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PASSWORD, null);
        return username != null && password != null
                && username.length() > 0 && password.length() > 0;
    }

    /**
     * Event callback for account deletion.
     */
    public interface DeleteAccountCallback
    {

        /**
         * Invoked if user opts to delete their account.
         */
        void onDeleteAccountStarted();

        /**
         * Invoked when the account has been deleted.
         */
        void onDeleteAccountEnded();

        /**
         * Invoked when there is an error deleting the account.
         *
         * @param message error message
         */
        void onDeleteAccountError(String message);
    }
}
