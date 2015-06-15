package ca.josephroque.partners.message;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

/**
 * Created by Joseph Roque on 2015-06-13.
 * <p/>
 * Service to handle messages from partner.
 */
public class MessageService
        extends Service
        implements SinchClientListener
{

    private static final String TAG = "MessageService";

    /** Represents result of client connection for intent. */
    private static final String CLIENT_RESULT = "clientConnectionResult";
    /** Represents client connection success. */
    private static final String CLIENT_SUCCESS = "clientConnectionSuccess";

    /** Remotable messaging service. */
    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    /** Instance of Sinch client. */
    private SinchClient mSinchClient;
    /** Instance of Sinch message client. */
    private MessageClient mMessageClient;
    /** Current user id being used to send messages. */
    private String mCurrentUserId;
    /** Instance of broadcast manager to notify application of events. */
    private LocalBroadcastManager mBroadcastManager;
    /** Intent to broadcast client results. */
    private Intent mBroadcastIntent = new Intent(CLIENT_RESULT);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "User: " + ParseUser.getCurrentUser());
        Log.i(TAG, "ObjectId: " + ParseUser.getCurrentUser().getObjectId());
        mCurrentUserId = ParseUser.getCurrentUser().getObjectId();

        if (mCurrentUserId != null && !isSinchClientStarted())
        {
            startSinchClient(mCurrentUserId);
        }

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Creates new instance of {@link SinchClient} with username as userId.
     *
     * @param username userId
     */
    public void startSinchClient(String username)
    {
        mSinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(SecretMessageUtil.APP_KEY)
                .applicationSecret(SecretMessageUtil.APP_SECRET)
                .environmentHost(SecretMessageUtil.ENVIRONMENT)
                .build();

        mSinchClient.addSinchClientListener(this);
        mSinchClient.setSupportMessaging(true);
        mSinchClient.setSupportActiveConnectionInBackground(true);
        mSinchClient.start();
    }

    /**
     * Checks if the {@code SinchClient} has been started.
     *
     * @return true if {@code mSinchClient} is not null and has started
     */
    private boolean isSinchClientStarted()
    {
        return mSinchClient != null && mSinchClient.isStarted();
    }

    @Override
    public void onClientFailed(SinchClient client, SinchError error)
    {
        mBroadcastIntent.putExtra(CLIENT_SUCCESS, false);
        mBroadcastManager.sendBroadcast(mBroadcastIntent);

        mSinchClient = null;
    }

    @Override
    public void onClientStarted(SinchClient client)
    {
        mBroadcastIntent.putExtra(CLIENT_SUCCESS, true);
        mBroadcastManager.sendBroadcast(mBroadcastIntent);

        client.startListeningOnActiveConnection();
        mMessageClient = client.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient client)
    {
        mSinchClient = null;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return serviceInterface;
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

    /**
     * Sends message to a recipient.
     *
     * @param recipientUserId recipient of message
     * @param textBody message
     */
    public void sendMessage(String recipientUserId, String textBody)
    {
        if (mMessageClient != null)
        {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            mMessageClient.send(message);
        }
    }

    /**
     * Adds a listener for messages to {@code mMessageClient}.
     *
     * @param listener message listener to add
     */
    public void addMessageClientListener(MessageClientListener listener)
    {
        if (mMessageClient != null)
        {
            mMessageClient.addMessageClientListener(listener);
        }
    }

    /**
     * Removes a message listener from {@code mMessageClient}.
     *
     * @param listener message listener to remove
     */
    public void removeMessageClientListener(MessageClientListener listener)
    {
        if (mMessageClient != null)
        {
            mMessageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy()
    {
        mSinchClient.stopListeningOnActiveConnection();
        mSinchClient.terminate();
    }

    /**
     * Remoteable object.
     */
    public class MessageServiceInterface
            extends Binder
    {

        /**
         * Calls method to send message to a recipient.
         *
         * @param recipientUserId recipient id
         * @param textBody message
         */
        public void sendMessage(String recipientUserId, String textBody)
        {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        /**
         * Calls method to add message listener to {@code mMessageClient}.
         *
         * @param listener listener to add
         */
        public void addMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.addMessageClientListener(listener);
        }

        /**
         * Calls method to remove listener from {@code mMessageClient}.
         *
         * @param listener listener to remove
         */
        public void removeMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.removeMessageClientListener(listener);
        }

        /**
         * Checks if the SinchClient has been started.
         *
         * @return true if {@code mSinchClient} is not null and has started
         */
        public boolean isSinchClientStarted()
        {
            return MessageService.this.isSinchClientStarted();
        }
    }
}
