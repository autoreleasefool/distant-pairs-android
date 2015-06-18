package ca.josephroque.partners.message;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

import ca.josephroque.partners.util.MessageUtil;

/**
 * Created by Joseph Roque on 2015-06-17.
 *
 * For sending and receiving messages through Sinch.
 */
public class MessageService
        extends Service
        implements SinchClientListener
{

    /** Key for connecting to Sinch API. */
    private static final String APP_KEY = "697a44cb-b77d-4255-a73b-236ec95214dc";
    /** Secret key for accessing Sinch API. */
    private static final String APP_SECRET = "nN4jRTaKvEeUcftdwdFEGQ==";
    /** Environment for Sinch API use. */
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    /** Remote binder for message service. */
    private final MessageServiceInterface mServiceInterface = new MessageServiceInterface();
    /** Instance of {@code SinchClient} for sending messages. */
    private SinchClient mSinchClient;
    /** Sinch {@code MessageClient} for sending messages. */
    private MessageClient messageClient;
    /** Parse user id. */
    private String mCurrentUserId;
    /** Instance of {@code LocalBroadcastManager}. */
    private LocalBroadcastManager mLocalBroadcastManager;
    /** Intent for indicating client status to application. */
    private Intent mMessageBroadcastIntent =
            new Intent(MessageUtil.CLIENT_STATUS);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mCurrentUserId = ParseUser.getCurrentUser().getObjectId();

        if (mCurrentUserId != null && !isSinchClientStarted())
            startSinchClient(mCurrentUserId);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Starts an instance of {@link SinchClient}.
     *
     * @param username username for sinch
     */
    public void startSinchClient(String username)
    {
        mSinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        mSinchClient.addSinchClientListener(this);
        mSinchClient.setSupportMessaging(true);
        mSinchClient.setSupportActiveConnectionInBackground(true);
        mSinchClient.start();
    }

    /**
     * Checks if {@code mSinchClient} is not null and started.
     *
     * @return true if {@code mSinchClient} has been started
     */
    private boolean isSinchClientStarted()
    {
        return mSinchClient != null && mSinchClient.isStarted();
    }

    @Override
    public void onClientFailed(SinchClient client, SinchError error)
    {
        mMessageBroadcastIntent.putExtra(MessageUtil.CLIENT_SUCCESS, false);
        mLocalBroadcastManager.sendBroadcast(mMessageBroadcastIntent);

        mSinchClient = null;
    }

    @Override
    public void onClientStarted(SinchClient client)
    {
        mMessageBroadcastIntent.putExtra(MessageUtil.CLIENT_SUCCESS, true);
        mLocalBroadcastManager.sendBroadcast(mMessageBroadcastIntent);

        client.startListeningOnActiveConnection();
        messageClient = client.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient client)
    {
        mSinchClient = null;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mServiceInterface;
    }

    @Override
    public void onLogMessage(int level, String area, String message)
    {
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client,
                                                  ClientRegistration clientRegistration)
    {
    }

    @Override
    public void onDestroy()
    {
        mSinchClient.stopListeningOnActiveConnection();
        mSinchClient.terminate();
    }

    /**
     * Sends a message to the recipient.
     *
     * @param recipientUserId recipient of message
     * @param textBody message
     */
    public void sendMessage(String recipientUserId, String textBody)
    {
        if (messageClient != null)
        {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }

    /**
     * Adds a listener for message events.
     *
     * @param listener listener to add
     */
    public void addMessageClientListener(MessageClientListener listener)
    {
        if (messageClient != null)
        {
            messageClient.addMessageClientListener(listener);
        }
    }

    /**
     * Removes a listener for message events.
     *
     * @param listener listener to remove
     */
    public void removeMessageClientListener(MessageClientListener listener)
    {
        if (messageClient != null)
        {
            messageClient.removeMessageClientListener(listener);
        }
    }

    /**
     * Remote binder for the messaging service.
     */
    public class MessageServiceInterface
            extends Binder
    {

        /**
         * Invokes method to send a message.
         *
         * @param recipientUserId recipient of message
         * @param textBody message
         * @see MessageService#sendMessage(String, String)
         */
        public void sendMessage(String recipientUserId, String textBody)
        {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        /**
         * Invokes method to add a listener for message events.
         *
         * @param listener listener to add
         * @see MessageService#addMessageClientListener(MessageClientListener)
         */
        public void addMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.addMessageClientListener(listener);
        }

        /**
         * Invokes method to remove a listener for message events.
         *
         * @param listener listener to remove
         * @see MessageService#removeMessageClientListener(MessageClientListener)
         */
        public void removeMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.removeMessageClientListener(listener);
        }

        /**
         * Invokes method to check if the {@link SinchClient} has started.
         *
         * @return {@link MessageService#isSinchClientStarted()}
         */
        public boolean isSinchClientStarted()
        {
            return MessageService.this.isSinchClientStarted();
        }
    }
}
