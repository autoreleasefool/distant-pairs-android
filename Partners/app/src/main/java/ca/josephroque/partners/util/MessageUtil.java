package ca.josephroque.partners.util;

/**
 * Created by Joseph Roque on 2015-06-18.
 *
 * Methods and constants related to identifying messages.
 */
public final class MessageUtil
{

    /** Represents broadcasted intent for indicating client status to application. */
    public static final String CLIENT_STATUS = "ca.josephroque.partners.client_success";
    /** Represents a successful or unsuccessful client connection. */
    public static final String CLIENT_SUCCESS = "message_success";

    /**
     * Default private constructor.
     */
    private MessageUtil()
    {
        // does nothing
    }
}
