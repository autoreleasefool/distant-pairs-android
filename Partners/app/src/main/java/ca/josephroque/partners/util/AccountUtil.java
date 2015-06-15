package ca.josephroque.partners.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Joseph Roque on 2015-06-13.
 * <p/>
 * Classes and methods for creating and managing an account.
 */
public final class AccountUtil
{

    /**
     * Default private constructor.
     */
    private AccountUtil()
    {
        // does nothing
    }

    /** Number of random bits to generate. */
    private static final int PASSWORD_BIT_LENGTH = 130;

    /** Number base. */
    private static final byte BASE = 32;

    /** Represents registration or login success. */
    public static final int ACCOUNT_SUCCESS = 0;
    /** Maximum length for a username. */
    public static final int USERNAME_MAX_LENGTH = 16;

    /** Represents password in preferences. */
    public static final String PASSWORD = "account_password";
    /** Represents account name in preferences. */
    public static final String USERNAME = "account_username";
    /** Represents pair's name in preferences. */
    public static final String PAIR = "account_pair";
    /** Represents parse object id for user and pair in server. */
    public static final String PARSE_PAIR_ID = "account_pair_id";

    /** Random number generator. */
    private static SecureRandom sSecureRandom = new SecureRandom();

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
     * Saves username and password to shared preferences.
     *
     * @param context to get shared preferences
     * @param accUser account username
     * @param accPass account password
     */
    public static void saveAccountCredentials(Context context, String accUser, String accPass)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USERNAME, accUser)
                .putString(PASSWORD, accPass)
                .commit();
    }

    /**
     * Saves pair's name to shared preferences.
     *
     * @param context to get shared preferences
     * @param partnerName pair's name
     */
    public static void savePairCredentials(Context context, String partnerName)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PAIR, partnerName)
                .commit();
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
     * Deletes the current user account.
     *
     * @param context to get shared preferences
     */
    public static void deleteAccount(Context context)
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
                .remove(PAIR)
                .remove(UserStatusUtil.STATUS_OBJECT_ID)
                .apply();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e != null)
                {
                    ParseObject.deleteAllInBackground(list);
                }
            }
        });
    }
}
