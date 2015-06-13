package ca.josephroque.partners.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Joseph Roque on 2015-06-13.
 * <p/>
 * To generate and store random passwords
 */
public final class PasswordUtil
{

    /**
     * Default private constructor.
     */
    private PasswordUtil()
    {
        // does nothing
    }

    /** Number of random bits to generate. */
    private static final int PASSWORD_BIT_LENGTH = 130;

    /** Number base. */
    private static final byte BASE = 32;

    /** Represents password in preferences. */
    public static final String PASSWORD = "account_password";

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
