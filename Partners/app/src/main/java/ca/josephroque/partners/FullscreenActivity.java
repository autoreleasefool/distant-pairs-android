package ca.josephroque.partners;

import ca.josephroque.partners.fragment.HeartFragment;
import ca.josephroque.partners.fragment.ThoughtFragment;
import ca.josephroque.partners.message.MessageService;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.UserStatusUtil;
import ca.josephroque.partners.util.hider.SystemUiHider;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity
        extends FragmentActivity
{

    private static final String TAG = "FullscreenActivity";

    /** Represents heart fragment location in view pager. */
    public static final byte HEART_FRAGMENT = 0;
    /** Represents thought fragment location in view pager. */
    public static final byte THOUGHT_FRAGMENT = 1;

    /** For posting tasks to hide the UI. */
    private Handler mHideHandler = new Handler();
    /** Hides the UI. */
    private Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mSystemUiHider.hide();
        }
    };

    /** Manages fragments displayed in the activity. */
    private ViewPager mViewPager;
    /** Manages fragments in view pager. */
    private FullscreenPagerAdapter mPagerAdapter;
    /** Begins instance of MessageService. */
    private Intent mMessageServiceIntent;

    private MessageService.MessageServiceInterface mMessageService;
    private ServiceConnection mServiceConnection = new MessageServiceConnection();
    private MessageClientListener mMessageClientListener = new MyMessageClientListener();

    /** Current fragment  of view pager. */
    private int mCurrentFragment = 0;

    /** Object id of partner (if null, partner is offline). */
    private String mPartnerId;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        if (savedInstanceState == null)
        {
            mPartnerId = getIntent().getExtras().getString(AccountUtil.PARSE_PAIR_ID);
        }
        else
        {
            mPartnerId = savedInstanceState.getString(AccountUtil.PARSE_PAIR_ID);
        }

        mMessageServiceIntent = new Intent(FullscreenActivity.this, MessageService.class);
        startService(mMessageServiceIntent);
        bindService(mMessageServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, SystemUiHider.HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener()
                {
                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible)
                    {
                        if (visible && SystemUiHider.AUTO_HIDE)
                        {
                            // Schedule a hide().
                            delayedHide(SystemUiHider.AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (SystemUiHider.TOGGLE_ON_CLICK)
                    mSystemUiHider.toggle();
                else
                    mSystemUiHider.show();
            }
        });

        setupViewPager();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(SystemUiHider.INITIAL_HIDE_DELAY);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (ParseUser.getCurrentUser() == null)
        {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.fullscreen_progressbar);
            final TextView textView = (TextView) findViewById(R.id.fullscreen_progress_text);

            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.GONE);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final String accountName = prefs.getString(AccountUtil.USERNAME, null);
            final String accountPassword = prefs.getString(AccountUtil.PASSWORD, null);

            if (accountName == null || accountPassword == null)
            {
                deleteAccount();
                return;
            }

            ParseUser.logInInBackground(accountName, accountPassword, new LogInCallback()
            {
                @Override
                public void done(ParseUser parseUser, ParseException e)
                {
                    final String statusId = prefs.getString(UserStatusUtil.STATUS_OBJECT_ID, null);

                    if (statusId == null)
                    {
                        final ParseObject statusObject = new ParseObject(UserStatusUtil.STATUS);
                        statusObject.put(UserStatusUtil.ONLINE_STATUS, true);
                        statusObject.put(AccountUtil.USERNAME, accountName);
                        statusObject.saveInBackground(new SaveCallback()
                        {
                            @Override
                            public void done(ParseException e)
                            {
                                if (e == null)
                                {
                                    prefs.edit().putString(UserStatusUtil.STATUS_OBJECT_ID,
                                            statusObject.getObjectId())
                                            .apply();

                                    checkIfPartnerOnline();

                                    progressBar.setVisibility(View.GONE);
                                    textView.setVisibility(View.GONE);
                                    mViewPager.setVisibility(View.VISIBLE);
                                }
                                // TODO: on error
                            }
                        });
                    }
                    else
                    {
                        ParseObject statusObject = new ParseObject(UserStatusUtil.STATUS);
                        statusObject.setObjectId(statusId);
                        statusObject.fetchInBackground(new GetCallback<ParseObject>()
                        {
                            @Override
                            public void done(ParseObject parseObject, ParseException e)
                            {
                                if (e == null)
                                {
                                    parseObject.put(UserStatusUtil.ONLINE_STATUS, true);
                                    parseObject.saveInBackground();

                                    checkIfPartnerOnline();

                                    progressBar.setVisibility(View.GONE);
                                    textView.setVisibility(View.GONE);
                                    mViewPager.setVisibility(View.VISIBLE);
                                }
                                // TODO: on error
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(AccountUtil.PARSE_PAIR_ID, mPartnerId);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (mPartnerId != null)
            mMessageService.sendMessage(mPartnerId, UserStatusUtil.LOGOUT_MESSAGE);

        final String statusId =
                PreferenceManager.getDefaultSharedPreferences(FullscreenActivity.this)
                        .getString(UserStatusUtil.STATUS_OBJECT_ID, null);
        if (statusId == null)
            return;

        final ParseObject statusObject = new ParseObject(UserStatusUtil.STATUS);
        statusObject.setObjectId(statusId);
        statusObject.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                statusObject.put(UserStatusUtil.ONLINE_STATUS, false);
                statusObject.saveInBackground();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopService(mMessageServiceIntent);
    }

    private void checkIfPartnerOnline()
    {
        String partnerName = PreferenceManager.getDefaultSharedPreferences(FullscreenActivity.this)
                .getString(AccountUtil.PAIR, null);
        if (partnerName == null)
            throw new IllegalArgumentException("invalid partner");

        ParseQuery<ParseObject> partnerQuery = new ParseQuery<>(UserStatusUtil.STATUS);
        partnerQuery.whereEqualTo(AccountUtil.USERNAME, partnerName);
        partnerQuery.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e == null)
                {
                    boolean partnerLoggedIn = list.get(0).getBoolean(UserStatusUtil.ONLINE_STATUS);
                    MessageDisplay currentFragment =
                            (MessageDisplay) mPagerAdapter.getCurrentFragment();
                    if (partnerLoggedIn)
                    {
                        mMessageService.sendMessage(mPartnerId, UserStatusUtil.LOGIN_MESSAGE);
                        currentFragment.displayMessage(UserStatusUtil.LOGIN_MESSAGE);
                    }
                    else
                    {
                        currentFragment.displayMessage(UserStatusUtil.LOGOUT_MESSAGE);
                    }
                }

                //TODO: on error
            }
        });

    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled
     * calls.
     *
     * @param delayMillis time before hiding UI
     */
    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Sets up view pager to manage fragments in the activity.
     */
    private void setupViewPager()
    {
        mViewPager = (ViewPager) findViewById(R.id.fullscreen_viewpager);
        mPagerAdapter = new FullscreenPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                mCurrentFragment = position;
            }
        });
    }

    /**
     * Deletes user account and returns to login activity.
     */
    public void deleteAccount()
    {
        AccountUtil.deleteAccount(this);
        startActivity(new Intent(FullscreenActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * Manages fragments displayed in the fullscreen activity.
     */
    private final class FullscreenPagerAdapter
            extends FragmentStatePagerAdapter
    {

        /** Array of initiated fragments. */
        private SparseArray<WeakReference<Fragment>> mRegisteredFragments = new SparseArray<>();

        /**
         * Defualt constructor, passes {@code fm} to super constructor.
         *
         * @param fm fragment manager
         */
        private FullscreenPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            Object object = super.instantiateItem(container, position);
            if (object instanceof Fragment)
            {
                mRegisteredFragments.put(position, new WeakReference<>((Fragment) object));
            }
            return object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            super.destroyItem(container, position, object);
            mRegisteredFragments.remove(position);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case HEART_FRAGMENT:
                    return HeartFragment.newInstance();
                case THOUGHT_FRAGMENT:
                    return ThoughtFragment.newInstance();
                default:
                    throw new IllegalArgumentException(
                            position + " is not valid position, max is " + (getCount() - 1));
            }
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        /**
         * Gets the current active fragment from {@code registeredFragments}.
         *
         * @return current fragment
         */
        public Fragment getCurrentFragment()
        {
            return mRegisteredFragments.get(mCurrentFragment).get();
        }
    }

    private class MessageServiceConnection
            implements ServiceConnection
    {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mMessageService = (MessageService.MessageServiceInterface) iBinder;
            mMessageService.addMessageClientListener(mMessageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mMessageService = null;
        }
    }

    private class MyMessageClientListener
            implements MessageClientListener
    {

        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo)
        {
            Snackbar.make(mViewPager, "Message failed to send.", Snackbar.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message)
        {
            if (message.getSenderId().equals(mPartnerId))
            {
                WritableMessage writableMessage =
                        new WritableMessage(message.getRecipientIds().get(0),
                                message.getTextBody());
                MessageDisplay currentFragment =
                        (MessageDisplay) mPagerAdapter.getCurrentFragment();
                currentFragment.displayMessage(writableMessage.getTextBody());
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId)
        {

            final WritableMessage writableMessage =
                    new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            if (UserStatusUtil.LOGIN_MESSAGE.equals(message.getTextBody())
                    || UserStatusUtil.LOGOUT_MESSAGE.equals(message.getTextBody()))
                return;

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
                            parseMessage.put("senderId", mPartnerId);
                            parseMessage.put("recipientId",
                                    writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
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

    public interface MessageDisplay
    {

        void displayMessage(String message);
    }
}
