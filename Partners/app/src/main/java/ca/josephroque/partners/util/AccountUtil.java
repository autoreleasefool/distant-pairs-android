package ca.josephroque.partners.util;

import java.math.BigInteger;
import java.security.SecureRandom;

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

    /** Represents password in preferences. */
    public static final String PASSWORD = "account_password";
    /** Represents account name in preferences. */
    public static final String USERNAME = "account_username";

    /** Random number generator. */
    private static SecureRandom sSecureRandom = new SecureRandom();

    /**
     * Generates a random 130 bit password.
     * @return random password
     */
    public static String randomAlphaNumericPassword()
    {
        return new BigInteger(PASSWORD_BIT_LENGTH, sSecureRandom).toString(BASE);
    }
}
