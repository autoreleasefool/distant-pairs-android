package ca.josephroque.partners.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Joseph Roque on 2015-06-18.
 *
 * Methods and constants related to identifying messages.
 */
public final class MessageUtil
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "MessageUtil";

    /** Represents broadcasted intent for indicating client status to application. */
    public static final String CLIENT_STATUS = "ca.josephroque.partners.client_success";
    /** Represents a successful or unsuccessful client connection. */
    public static final String CLIENT_SUCCESS = "message_success";

    /** Parse object identifier for user's online status. */
    public static final String STATUS = "Status";
    /** Represents a boolean indicating user's online status. */
    public static final String ONLINE_STATUS = "status_online";
    /** Represents Parse object id for status object. */
    public static final String STATUS_OBJECT_ID = "status_object_id";

    /** A login message. */
    public static final String LOGIN_MESSAGE = "~LOGIN";
    /** A logout message. */
    public static final String LOGOUT_MESSAGE = "~LOGOUT";

    /** Maximum length of messages. */
    public static final int MAX_MESSAGE_LENGTH = 140;

    /** To format a {@link Date} object to string. */
    private static SimpleDateFormat sDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);

    /**
     * Default private constructor.
     */
    private MessageUtil()
    {
        // does nothing
    }

    /**
     * Gets the current date and time, formatted 'yyyy-MM-dd HH:mm:ss'.
     *
     * @return current date and time
     */
    public static String getCurrentDateAndTime()
    {
        return sDateFormat.format(new Date());
    }

    /**
     * Formats a {@link Date} object to a string.
     * @param date to format
     * @return string of the format 'yyyy-MM-dd HH:mm:ss'
     */
    public static String formatDate(Date date)
    {
        return sDateFormat.format(date);
    }
}
