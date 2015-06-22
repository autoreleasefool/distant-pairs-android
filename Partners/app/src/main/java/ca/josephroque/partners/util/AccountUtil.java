package ca.josephroque.partners.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ca.josephroque.partners.R;

/**
 * Created by Joseph Roque on 2015-06-16.
 *
 * Classes and methods for creating and managing an account.
 */
public final class AccountUtil
{

    /** Indicates if the user's account is being deleted in the background. */
    private static AtomicBoolean sAccountBeingDeleted = new AtomicBoolean(false);

    /** Represents password in preferences. */
    public static final String PASSWORD = "account_password";
    /** Represents account name in preferences. */
    public static final String USERNAME = "account_username";
    /** Represents pair's name in preferences. */
    public static final String PAIR = "account_pair";
    /** Represents parse object containing the user's registered pair. */
    public static final String PARSE_PAIR_ID = "account_pair_objid";

    /** Number of random bits to generate. */
    private static final int PASSWORD_BIT_LENGTH = 130;
    /** Maximum character length for usernames. */
    public static final int USERNAME_MAX_LENGTH = 16;
    /** Represents successful account related operation. */
    public static final int SUCCESS = 0;

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
                    deleteAccount(context);
                    if (callback != null)
                        callback.onDeleteAccount();
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
     */
    public static void deleteAccount(Context context)
    {
        sAccountBeingDeleted.set(true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null)
        {
            ParseUser.logOutInBackground(new LogOutCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e != null)
                        sAccountBeingDeleted.set(false);
                    // TODO: error handling
                }
            });
        }

        String username = preferences.getString(USERNAME, null);
        if (username == null)
            return;

        ParseQuery<ParseObject> pairQuery = ParseQuery.or(Arrays.asList(
                new ParseQuery<>("Pair").whereEqualTo(USERNAME, username),
                new ParseQuery<>("Pair").whereEqualTo(PAIR, username)));
        ParseQuery<ParseObject> statusQuery = ParseQuery.getQuery("Status");
        statusQuery.whereEqualTo(AccountUtil.USERNAME, username);
        // TODO: get other objects with user's name

        preferences.edit()
                .remove(USERNAME)
                .remove(PASSWORD)
                .remove(PAIR)
                .remove(PARSE_PAIR_ID)
                .apply();

        pairQuery.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e != null)
                    ParseObject.deleteAllInBackground(list);
            }
        });
        statusQuery.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e != null)
                    ParseObject.deleteAllInBackground(list);
                // TODO: error handling
            }
        });
    }

    /**
     * Returns true if an account is currently being deleted in the background.
     *
     * @return {@code sAccountBeingDeleted}
     */
    public static boolean isAccountBeingDeleted()
    {
        return sAccountBeingDeleted.get();
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
     * Event callback for account deletion.
     */
    public interface DeleteAccountCallback
    {

        /**
         * Invoked if user opts to delete their account.
         */
        void onDeleteAccount();
    }

    /**
     * Event callback for pair retrieval.
     */
    public interface PairCallback
    {

        /**
         * Invoked when the user's pair object id is available.
         *
         * @param pairId Parse object id of pair
         */
        void onPairIdAvailable(String pairId, int errorCode);
    }
}
