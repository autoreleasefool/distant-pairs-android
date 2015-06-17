package ca.josephroque.partners.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Arrays;
import java.util.List;

import ca.josephroque.partners.R;

/**
 * Created by Joseph Roque on 2015-06-16.
 *
 * Classes and methods for creating and managing an account.
 */
public final class AccountUtil
{

    /** Maximum character length for usernames. */
    public static final int USERNAME_MAX_LENGTH = 16;
    /** Represents password in preferences. */
    public static final String PASSWORD = "account_password";
    /** Represents account name in preferences. */
    public static final String USERNAME = "account_username";
    /** Represents pair's name in preferences. */
    public static final String PAIR = "account_pair";

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
     * Prompt user to delete their account.
     *
     * @param context to create dialog
     */
    public static void promptDeleteAccount(final Context context)
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
    private static void deleteAccount(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String username = preferences.getString(USERNAME, null);
        if (username == null)
            return;

        ParseQuery<ParseObject> query = ParseQuery.or(Arrays.asList(
                new ParseQuery<>("Pair").whereEqualTo(USERNAME, username),
                new ParseQuery<>("Pair").whereEqualTo(PAIR, username),
                new ParseQuery<>("Status").whereEqualTo(AccountUtil.USERNAME, username)));
        // TODO: get other objects with user's name

        preferences.edit()
                .remove(USERNAME)
                .remove(PASSWORD)
                .remove(PAIR)
                .apply();

        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e != null)
                {
                    ParseObject.deleteAllInBackground(list);
                }
                // TODO: error handling
            }
        });
    }
}
