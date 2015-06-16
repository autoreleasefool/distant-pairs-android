package ca.josephroque.partners.util;

/**
 * Created by Joseph Roque on 2015-06-16.
 *
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

    /**
     * Checks if username is valid and, if it is, removes uppercase letters.
     * @param username username to validate
     * @return {@code username} without uppercase letter if it contains only letters and numbers,
     * or null if the username is otherwise invalid.
     */
    public static String validateUsername(String username)
    {
        if (!username.matches("^[a-zA-Z0-9]+$"))
            return null;

        return username.toLowerCase();
    }
}
