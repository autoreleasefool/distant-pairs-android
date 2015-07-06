package ca.josephroque.partners.message;

/**
 * Created by Joseph Roque on 2015-06-21. Provides methods for fragments to handle new messages.
 */
public interface MessageHandler
{

    /**
     * Called when a new message has been received.
     *
     * @param messageId unique identifier for message
     * @param dateAndTime time the message was sent - yyyy-MM-dd HH:mm:ss
     * @param message new message
     */
    void onNewMessage(String messageId, String dateAndTime, String message);
}
