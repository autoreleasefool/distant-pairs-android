package ca.josephroque.partners.interfaces;

/**
 * Created by Joseph Roque on 2015-06-21.
 *
 * Provides methods for fragments to handle new messages.
 */
public interface MessageHandler
{

    /**
     * Called when a new message has been received.
     *
     * @param dateAndTime time the message was received - yyyy-MM-dd HH:mm:ss
     * @param message new message
     */
    void onNewMessage(String dateAndTime, String message);
}
