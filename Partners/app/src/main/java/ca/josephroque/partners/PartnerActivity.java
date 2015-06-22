package ca.josephroque.partners;

import ca.josephroque.partners.fragment.HeartFragment;
import ca.josephroque.partners.fragment.RegisterFragment;
import ca.josephroque.partners.fragment.ThoughtFragment;
import ca.josephroque.partners.interfaces.ActionButtonHandler;
import ca.josephroque.partners.interfaces.MessageHandler;
import ca.josephroque.partners.message.MessageService;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;
import ca.josephroque.partners.util.MessageUtil;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;


/**
 * Main activity for user interaction.
 */
public class PartnerActivity
        extends FragmentActivity
        implements View.OnClickListener, RegisterFragment.RegisterCallbacks
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "PartnerActivity";

    /** Represents current fragment in the view pager. */
    private static final String ARG_CURRENT_FRAGMENT = "arg_cur_frag";
    /** Represents boolean indicating if the user has a pair registered. */
    private static final String ARG_PAIR_REGISTERED = "arg_pair_reg";
    /** Represents boolean indicating if the app should attempt to log the user in. */
    public static final String ARG_ATTEMPT_LOGIN = "arg_attempt_login";
    /** Position of HeartFragment instance in ViewPager. */
    public static final byte HEART_FRAGMENT = 0;
    /** Position of ThoughtFragment instance in ViewPager. */
    public static final byte THOUGHT_FRAGMENT = 1;

    /** Center pivot for scale animation. */
    private static final float CENTER_PIVOT = 0.5f;

    /** Displays a spinning progress dialog. */
    private ProgressDialog mProgressDialogMessageService;
    /** Receives intent to hide spinning progress dialog. */
    private BroadcastReceiver mReceiverMessageService = null;
    /** Intent to initiate instance of {@link MessageService}. */
    private Intent mIntentMessageService;
    /** Instanace of service connection. */
    private ServiceConnection mServiceConnection = new MessageServiceConnection();
    /** Instance of service interface. */
    private MessageService.MessageServiceInterface mMessageService;
    /** Instance of message listener. */
    private MessageClientListener mMessageClientListener = new PartnerMessageClientListener();

    /** Floating Action Button for primary action in the current fragment. */
    private FloatingActionButton mFabPrimary;
    /** View pager for fragments. */
    private ViewPager mViewPager;
    /** Adapter to manage fragments displayed by this activity. */
    private PartnerPagerAdapter mPagerAdapter;

    /** Counts the number of times a message has failed to send. */
    private HashMap<String, Integer> mFailedMessageCount;

    /** Parse object id of partner. */
    private String mPairId;
    /** Indicates if the user has registered a pair. */
    private boolean mIsPairRegistered = false;
    /** The current position of the view pager. */
    private int mCurrentViewPagerPosition = 0;
    /** Id of the current icon of {@code mFabPrimary}. */
    private int mCurrentFabIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner);

        mFailedMessageCount = new HashMap<>();

        mIntentMessageService = new Intent(PartnerActivity.this, MessageService.class);
        startService(mIntentMessageService);
        bindService(mIntentMessageService, mServiceConnection, BIND_AUTO_CREATE);

        mFabPrimary = (FloatingActionButton) findViewById(R.id.fab_partner);
        mFabPrimary.setOnClickListener(this);

        mViewPager = (ViewPager) findViewById(R.id.vp_partner);
        mPagerAdapter = new PartnerPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                mCurrentViewPagerPosition = position;
                updateFloatingActionButton();
            }
        });

        if (savedInstanceState != null)
        {
            mIsPairRegistered = savedInstanceState.getBoolean(ARG_PAIR_REGISTERED, false);
            mCurrentViewPagerPosition = savedInstanceState.getInt(ARG_CURRENT_FRAGMENT);
            mViewPager.setCurrentItem(mCurrentViewPagerPosition);
        }
        else
        {
            mIsPairRegistered = AccountUtil.doesPartnerExist(this);
        }
        mPagerAdapter.notifyDataSetChanged();

        updateFloatingActionButton();
        showServiceSpinner();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_CURRENT_FRAGMENT, mCurrentViewPagerPosition);
        outState.putBoolean(ARG_PAIR_REGISTERED, mIsPairRegistered);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverMessageService);
        stopService(mIntentMessageService);
    }

    @Override
    public void onClick(View src)
    {
        switch (src.getId())
        {
            case R.id.fab_partner:
                ((ActionButtonHandler) mPagerAdapter.getCurrentFragment())
                        .handleActionClick();
                break;
            default:
                //does nothing
        }
    }

    @Override
    public void login(RegisterFragment.LoginCallback callback)
    {
        throw new UnsupportedOperationException("already logged in");
    }

    @Override
    public void pairRegistered()
    {
        mPairId = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(AccountUtil.PARSE_PAIR_ID, null);
        mIsPairRegistered = true;
        mPagerAdapter.notifyDataSetChanged();
        updateFloatingActionButton();
    }

    /**
     * Prompts user to delete their account.
     *
     * @see AccountUtil#promptDeleteAccount(Context, AccountUtil.DeleteAccountCallback)
     */
    public void deleteAccount()
    {
        AccountUtil.promptDeleteAccount(this,
                new AccountUtil.DeleteAccountCallback()
                {
                    @Override
                    public void onDeleteAccount()
                    {
                        stopService(mIntentMessageService);
                        Intent loginIntent = new Intent(PartnerActivity.this,
                                LoginActivity.class);
                        loginIntent.putExtra(ARG_ATTEMPT_LOGIN, false);
                        startActivity(loginIntent);
                        finish();
                    }
                });
    }

    /**
     * Displays a progress dialog and waits for the message service to start.
     */
    private void showServiceSpinner()
    {
        mProgressDialogMessageService = new ProgressDialog(this);
        mProgressDialogMessageService.setTitle(R.string.text_loading);
        mProgressDialogMessageService.setIndeterminate(true);

        mReceiverMessageService = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                boolean success = intent.getBooleanExtra(MessageUtil.CLIENT_SUCCESS, false);
                mProgressDialogMessageService.dismiss();
                if (!success)
                {
                    ErrorUtil.displayErrorMessage(PartnerActivity.this, "Service failure",
                            "Messaging service failed to start.");
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverMessageService,
                new IntentFilter(MessageUtil.CLIENT_STATUS));
    }

    /**
     * Sets icon of the floating action button depending on the current fragment.
     */
    private void updateFloatingActionButton()
    {
        final int newDrawableId;
        if (mIsPairRegistered)
            newDrawableId = R.drawable.ic_cloud;
        else
            newDrawableId = R.drawable.ic_close;

        if (newDrawableId != mCurrentFabIcon)
        {
            final int animTime =
                    getResources().getInteger(android.R.integer.config_mediumAnimTime);
            ScaleAnimation shrink = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                    Animation.RELATIVE_TO_SELF, CENTER_PIVOT, Animation.RELATIVE_TO_SELF,
                    CENTER_PIVOT);
            shrink.setDuration(animTime);
            shrink.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {
                    // does nothing
                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    mFabPrimary.setImageResource(newDrawableId);
                    mCurrentFabIcon = newDrawableId;
                    ScaleAnimation grow = new ScaleAnimation(0f, 1.0f, 0f, 1.0f,
                            Animation.RELATIVE_TO_SELF, CENTER_PIVOT, Animation.RELATIVE_TO_SELF,
                            CENTER_PIVOT);
                    grow.setDuration(animTime);
                    grow.setInterpolator(new OvershootInterpolator());
                    mFabPrimary.setAnimation(grow);
                    grow.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {
                    // does nothing
                }
            });

            mFabPrimary.setAnimation(shrink);
            shrink.start();
        }
    }

    /**
     * Switches to a certain fragment in the view pager.
     *
     * @param fragment position to switch to
     */
    public void showFragment(byte fragment)
    {
        mViewPager.setCurrentItem(fragment);
    }

    /**
     * Sends a message to the user's pair.
     *
     * @param message message for pair
     */
    public void sendMessage(String message)
    {
        // TODO: check for valid message
        mMessageService.sendMessage(mPairId, message);
    }

    /**
     * Adapter for managing views in view pager.
     */
    private final class PartnerPagerAdapter
            extends FragmentStatePagerAdapter
    {

        /** Fragments in the view pager. */
        private SparseArray<WeakReference<Fragment>> mRegisteredFragments = new SparseArray<>();

        /**
         * Default constructor.
         *
         * @param fm fragment mananger
         */
        private PartnerPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case HEART_FRAGMENT:
                    if (mIsPairRegistered)
                        return HeartFragment.newInstance();
                    else
                        return RegisterFragment.newInstance(false);
                case THOUGHT_FRAGMENT:
                    return ThoughtFragment.newInstance();
                default:
                    throw new IllegalStateException("invalid view pager position: " + position);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment)
            {
                Fragment fragment = (Fragment) obj;
                mRegisteredFragments.put(position, new WeakReference<>(fragment));
            }
            return obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object item)
        {
            super.destroyItem(container, position, item);
            if (item instanceof Fragment)
                mRegisteredFragments.remove(position);
        }

        @Override
        public int getCount()
        {
            if (mIsPairRegistered)
                return 2;
            else
                return 1;
        }

        /**
         * Gets the fragment registered at a certain position, or null if one does not exist.
         *
         * @param position position of fragment
         * @return fragment at {@code position}
         */
        private Fragment getFragment(int position)
        {
            return mRegisteredFragments.get(position).get();
        }

        /**
         * Gets the currently visible fragment in the view pager.
         *
         * @return fragment at position {@code mCurrentViewPagerPosition}
         */
        private Fragment getCurrentFragment()
        {
            return getFragment(mCurrentViewPagerPosition);
        }
    }

    /**
     * Handles a messaging service connection.
     */
    private class MessageServiceConnection
            implements ServiceConnection
    {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            Log.i(TAG, "Service connected");
            mMessageService = (MessageService.MessageServiceInterface) iBinder;
            mMessageService.addMessageClientListener(mMessageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mMessageService = null;
        }
    }

    /**
     * Listens for message events.
     */
    private class PartnerMessageClientListener
            implements MessageClientListener
    {

        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo)
        {
            final String messageText = message.getTextBody();
            if (MessageUtil.LOGIN_MESSAGE.equals(messageText)
                    || MessageUtil.LOGOUT_MESSAGE.equals(messageText))
                return;

            Integer failureCount = mFailedMessageCount.get(messageText);
            mFailedMessageCount.put(messageText, ((failureCount != null)
                    ? failureCount
                    : 0) + 1);

            if (failureCount != null && failureCount > 1)
            {
                Snackbar.make(findViewById(R.id.cl_partner), "Failed to send message.",
                        Snackbar.LENGTH_SHORT)
                        .setAction("Resend", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                sendMessage(messageText);
                            }
                        })
                        .show();
            }
            else
            {
                Snackbar.make(findViewById(R.id.cl_partner), "Message failed too many times.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message)
        {
            Fragment currentFragment = mPagerAdapter.getCurrentFragment();
            if (currentFragment instanceof MessageHandler)
                ((MessageHandler) mPagerAdapter.getCurrentFragment()).onNewMessage(
                        MessageUtil.formatDate(message.getTimestamp()), message.getTextBody());
        }

        @Override
        public void onMessageSent(MessageClient client, final Message message, String recipientId)
        {
            final String messageText = message.getTextBody();
            final String messageTime = MessageUtil.formatDate(message.getTimestamp());

            if (MessageUtil.LOGIN_MESSAGE.equals(messageText)
                    || MessageUtil.LOGOUT_MESSAGE.equals(messageText))
                return;

            mFailedMessageCount.remove(messageText);

            Log.i(TAG, "Saving message to parse: " + message.getTextBody());

            //only add message to parse database if it doesn't already exist there
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e)
                {
                    if (e == null)
                    {
                        if (messageList.size() == 0)
                        {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", mPairId);
                            parseMessage.put("recipientId", message.getRecipientIds().get(0));
                            parseMessage.put("messageText", messageText);
                            parseMessage.put("sinchId", message.getMessageId());
                            parseMessage.put("sentTime", messageTime);
                            parseMessage.saveInBackground();
                        }
                    }
                }
            });
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo)
        {
            // does nothing
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message,
                                         List<PushPair> pushPairs)
        {
            // does nothing
        }
    }
}
