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

/**
 * Created by Joseph Roque on 2015-06-13.
 * <p/>
 * Service to handle messages from partner.
 */
public class MessageService
        extends Service
        implements SinchClientListener
{

    private static final String CLIENT_RESULT = "clientConnectionResult";
    private static final String CLIENT_SUCCESS = "clientConnectionSuccess";

    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient mSinchClient;
    private MessageClient mMessageClient;
    private String mCurrentUserId;
    private LocalBroadcastManager mBroadcastManager;
    private Intent mBroadcastIntent = new Intent(CLIENT_RESULT);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mCurrentUserId = ParseUser.getCurrentUser().getObjectId();

        if (mCurrentUserId != null && !isSinchClientStarted())
        {
            startSinchClient(mCurrentUserId);
        }

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        return super.onStartCommand(intent, flags, startId);
    }

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

    public void sendMessage(String recipientUserId, String textBody)
    {
        if (mMessageClient != null)
        {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            mMessageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener)
    {
        if (mMessageClient != null)
        {
            mMessageClient.addMessageClientListener(listener);
        }
    }

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

    public class MessageServiceInterface
            extends Binder
    {

        public void sendMessage(String recipientUserId, String textBody)
        {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted()
        {
            return MessageService.this.isSinchClientStarted();
        }
    }
}
