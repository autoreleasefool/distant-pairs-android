package ca.josephroque.partners.util;

import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.josephroque.partners.R;

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
    /** Number of characters at the start of a message which indicate its type. */
    public static final int MESSAGE_TYPE_RESERVED_LENGTH = 4;
    /** If found at the start of a message, indicates the message describes an error. */
    public static final String MESSAGE_TYPE_ERROR = "ERR";
    /** If found at the start of a message, indicates the message can be sent to a partner. */
    public static final String MESSAGE_TYPE_VALID = "MSG";

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
     *
     * @param date to format
     * @return string of the format 'yyyy-MM-dd HH:mm:ss'
     */
    public static String formatDate(Date date)
    {
        return sDateFormat.format(date);
    }

    /**
     * Checks to see if the string is a valid message to send as a thought, and if not a string of
     * the format 'ERR:X' is returned, where X is the id of a string.
     *
     * @param message message to check
     * @return if {@code message} is valid, a String formatted 'MSG:message'. Otherwise, returns a
     * String formatted 'ERR:X'
     */
    public static String getValidMessage(String message)
    {
        if (LOGIN_MESSAGE.equals(message) || LOGOUT_MESSAGE.equals(message))
            return MESSAGE_TYPE_VALID + ":" + message;

        if (message == null || message.length() == 0)
            return MESSAGE_TYPE_ERROR + ":" + R.string.text_no_message + ":" + message;
        else if (message.length() > MAX_MESSAGE_LENGTH)
            return MESSAGE_TYPE_ERROR + ":" + R.string.text_message_too_long
                    + ":" + message;
            // TODO: create regex for valid message characters
        else if (!message.matches("[a-zA-Z0-9 ]"))
            return MESSAGE_TYPE_ERROR + ":" + R.string.text_message_invalid_characters
                    + ":" + message;
        else
            return MESSAGE_TYPE_VALID + ":" + message;
    }

    /**
     * Displays a snackbar with the error message.
     *
     * @param rootView view for snackbar
     * @param error error message
     */
    public static void handleError(View rootView, String error)
    {
        final int errorId;
        final String message;
        try
        {
            final String[] errorComponents = error.split(":", 3);
            if (!errorComponents[0].equals(MESSAGE_TYPE_ERROR))
                throw new IllegalArgumentException();
            errorId = Integer.parseInt(errorComponents[1]);
            message = errorComponents[2];
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error message must follow format 'ERR:X:message' where X is an integer and"
                    + "message is the message that caused the error");
            return;
        }

        final String errorMessage = rootView.getContext().getResources().getString(errorId);

        // TODO: check message to figure out what error was
        ErrorUtil.displayErrorSnackbar(rootView, errorMessage);
    }
}
