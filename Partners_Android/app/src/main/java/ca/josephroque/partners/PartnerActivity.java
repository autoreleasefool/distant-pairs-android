package ca.josephroque.partners;

import ca.josephroque.partners.fragment.HeartFragment;
import ca.josephroque.partners.fragment.RegisterFragment;
import ca.josephroque.partners.fragment.ThoughtFragment;
import ca.josephroque.partners.message.MessageHandler;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.AnimationUtil;
import ca.josephroque.partners.util.DisplayUtil;
import ca.josephroque.partners.util.ErrorUtil;
import ca.josephroque.partners.util.MessageUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;


/**
 * Main activity for user interaction.
 */
public class PartnerActivity
        extends ProgressActivity
        implements View.OnClickListener, RegisterFragment.RegisterCallbacks,
        ThoughtFragment.ThoughtFragmentCallbacks
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "PartnerActivity";

    /** Represents current fragment in the view pager. */
    private static final String ARG_CURRENT_FRAGMENT = "arg_cur_frag";
    /** Represents boolean indicating if the user has a pair registered. */
    private static final String ARG_PAIR_REGISTERED = "arg_pair_reg";
    /** Position of HeartFragment instance in ViewPager. */
    public static final byte HEART_FRAGMENT = 0;
    /** Position of ThoughtFragment instance in ViewPager. */
    public static final byte THOUGHT_FRAGMENT = 1;
    /** Represents boolean indicating the partner's online status. */
    private static final String ARG_PARTNER_ONLINE = "arg_partner_online";

    /** Center pivot for scale animation. */
    private static final float CENTER_PIVOT = 0.5f;

    /** Broadcast receiver for messages from partner. */
    private BroadcastReceiver mMessageBroadcastReceiver;

    /** Primary coordinator layout. */
    private CoordinatorLayout mCoordinatorLayout;
    /** Floating Action Button for primary action in the current fragment. */
    private FloatingActionButton mFabPrimary;
    /** View pager for fragments. */
    private ViewPager mViewPager;
    /** Adapter to manage fragments displayed by this activity. */
    private PartnerPagerAdapter mPagerAdapter;

    /** Displays progress when connecting to server. */
    private LinearLayout mLinearLayoutProgress;
    /** Displays action when connecting to server. */
    private TextView mTextViewProgress;

    /** Images of hearts to animate. */
    private ImageView[] mImageViewHearts;
    /** Image of soft glow to indicate user logging in. */
    private ImageView mImageViewLoginGlow;

    /** Counts the number of times a message has failed to send. */
    private HashMap<String, Integer> mFailedMessageCount;

    /** Name of user. */
    private String mUsername;
    /** Username of partner. */
    private String mPartnerName;
    /** Indicates if the user has registered a pair. */
    private boolean mIsPairRegistered;
    /** The current position of the view pager. */
    private int mCurrentViewPagerPosition = 0;
    /** Id of the current icon of {@code mFabPrimary}. */
    private int mCurrentFabIcon;
    /** Tracks number of attempts made to appear online. */
    private int mStatusAttemptCount;
    /** Indicates if the user's partner is online. */
    private boolean mIsPartnerOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner);

        mFailedMessageCount = new HashMap<>();

        final int underLollipopMargin = 8;
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.cl_partner);
        mFabPrimary = (FloatingActionButton) findViewById(R.id.fab_partner);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            ViewGroup.MarginLayoutParams p =
                    (ViewGroup.MarginLayoutParams) mFabPrimary.getLayoutParams();
            p.setMargins(0, 0, DisplayUtil.convertDpToPx(this, underLollipopMargin), 0);
            mFabPrimary.setLayoutParams(p);
        }
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
            mIsPartnerOnline = savedInstanceState.getBoolean(ARG_PARTNER_ONLINE, false);
            mIsPairRegistered = savedInstanceState.getBoolean(ARG_PAIR_REGISTERED, false);
            mCurrentViewPagerPosition = savedInstanceState.getInt(ARG_CURRENT_FRAGMENT);
            mViewPager.setCurrentItem(mCurrentViewPagerPosition);
        }
        else
        {
            mIsPairRegistered = AccountUtil.doesPartnerExist(this);
        }
        mPagerAdapter.notifyDataSetChanged();

        if (mIsPairRegistered)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mPartnerName = preferences.getString(AccountUtil.PAIR, null);
            mUsername = preferences.getString(AccountUtil.USERNAME, null);
        }

        mImageViewLoginGlow = (ImageView) findViewById(R.id.iv_login_glow);

        updateFloatingActionButton();
        populateHeartImageViews();
        registerMessageReceiver();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setOnlineStatus(true);
        checkIfPartnerOnline();
        checkForPartnerLogins();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        setOnlineStatus(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_CURRENT_FRAGMENT, mCurrentViewPagerPosition);
        outState.putBoolean(ARG_PAIR_REGISTERED, mIsPairRegistered);
        outState.putBoolean(ARG_PARTNER_ONLINE, mIsPartnerOnline);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageBroadcastReceiver);
    }

    @Override
    public void onClick(View src)
    {
        switch (src.getId())
        {
            case R.id.fab_partner:
                if (mIsPairRegistered)
                    displayThoughtDialog();
                else
                    deleteAccount();
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPartnerName = preferences.getString(AccountUtil.PAIR, null);
        mUsername = preferences.getString(AccountUtil.USERNAME, null);
        mIsPairRegistered = true;
        mPagerAdapter.notifyDataSetChanged();
        setOnlineStatus(true);
        updateFloatingActionButton();
    }

    @Override
    public void setMostRecentThought(String message, String timestamp)
    {
        Fragment fragment = mPagerAdapter.getFragment(0);
        if (fragment instanceof HeartFragment)
            ((HeartFragment) fragment).setMostRecentThought(message, timestamp);
    }

    /**
     * Displays a dialog for the user to send a thought to their partner.
     */
    private void displayThoughtDialog()
    {
        if (!mIsPartnerOnline && MessageUtil.wasThoughtSent(this))
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.text_message_already_sent)
                    .setMessage(R.string.text_message_already_sent_content)
                    .setPositiveButton(R.string.text_dialog_okay,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            })
                    .create()
                    .show();
            return;
        }

        View view = View.inflate(this, R.layout.dialog_thought, null);
        final TextView textViewLimit = (TextView) view.findViewById(R.id.tv_message_limit);
        final EditText editTextMessage = (EditText) view.findViewById(R.id.et_thought);
        editTextMessage.addTextChangedListener(new ThoughtWatcher(textViewLimit));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                    sendMessage(editTextMessage.getText().toString());
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.text_send_message)
                .setView(view)
                .setPositiveButton(R.string.text_dialog_send, listener)
                .setNegativeButton(R.string.text_dialog_cancel, listener)
                .create()
                .show();
    }

    /**
     * Registers a custom {@link android.content.BroadcastReceiver} in the {@link
     * android.support.v4.content.LocalBroadcastManager}.
     */
    private void registerMessageReceiver()
    {
        mMessageBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String message = intent.getStringExtra("message");
                String uniqueId = intent.getStringExtra("id");
                String timestamp = intent.getStringExtra("timestamp");

                Fragment fragment = mPagerAdapter.getFragment(0);
                if (fragment instanceof MessageHandler)
                    ((MessageHandler) fragment).onNewMessage(uniqueId, timestamp, message);
                try
                {
                    fragment = mPagerAdapter.getFragment(1);
                    if (fragment instanceof MessageHandler)
                        ((MessageHandler) fragment).onNewMessage(uniqueId, timestamp, message);
                }
                catch (NullPointerException ex)
                {
                    // does nothing
                }

                if (MessageUtil.LOGIN_MESSAGE.equals(message))
                {
                    setPartnerOnline(true);
                    loginGlowAnimation();
                }
                else if (!MessageUtil.LOGOUT_MESSAGE.equals(message))
                    superCuteHeartAnimation();
                else
                    setPartnerOnline(false);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageBroadcastReceiver,
                new IntentFilter(MessageUtil.ACTION_MESSAGE_RECEIVED));
    }

    /**
     * Creates an array of ImageView objects for {@code mImageViewHearts}.
     */
    @SuppressWarnings("deprecation")        // Newer methods used on newer api, deprecated on old
    private void populateHeartImageViews()
    {
        final int deviceWidth = getResources().getDisplayMetrics().widthPixels;
        final int numberOfHearts = (int) (deviceWidth / AnimationUtil.HEART_RATIO * 10);
        final int numberOfLargeHearts = numberOfHearts / AnimationUtil.HEART_SIZE_RATIO;

        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.rl_partner);
        mImageViewHearts = new ImageView[numberOfHearts];

        for (int i = 0; i < numberOfHearts; i++)
        {
            mImageViewHearts[i] = new ImageView(this);
            mImageViewHearts[i].setVisibility(View.GONE);
            mImageViewHearts[i].setAdjustViewBounds(true);
            if (i < numberOfLargeHearts)
                mImageViewHearts[i].setImageResource(R.drawable.ic_heart_large);
            else
                mImageViewHearts[i].setImageResource(R.drawable.ic_heart_med);
            rootLayout.addView(mImageViewHearts[i]);
        }
    }

    /**
     * Sets whether partner is online or not.
     *
     * @param online new value for {@code mIsPartnerOnline}
     */
    private void setPartnerOnline(boolean online)
    {
        this.mIsPartnerOnline = online;
    }

    /**
     * Prompts user to delete their account.
     *
     * @see AccountUtil#promptDeleteAccount(android.content.Context,
     * AccountUtil.DeleteAccountCallback)
     */
    public void deleteAccount()
    {
        AccountUtil.promptDeleteAccount(this,
                new AccountUtil.DeleteAccountCallback()
                {
                    @Override
                    public void onDeleteAccountStarted()
                    {
                        sendMessage(MessageUtil.LOGOUT_MESSAGE);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showProgressBar(R.string.text_deleting_account);
                            }
                        });

                    }

                    @Override
                    public void onDeleteAccountEnded()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Intent loginIntent =
                                        new Intent(PartnerActivity.this, SplashActivity.class);
                                startActivity(loginIntent);
                                finish();
                            }
                        });

                    }

                    @Override
                    public void onDeleteAccountError(String message)
                    {
                        if (message != null)
                            ErrorUtil.displayErrorDialog(PartnerActivity.this,
                                    "Error deleting account", message);
                        else
                            ErrorUtil.displayErrorDialog(PartnerActivity.this,
                                    "Error deleting account", "An unknown error occurred and you"
                                            + " may not be able to use this username again.");
                        onDeleteAccountEnded();
                    }
                });
    }

    @Override
    public void showProgressBar(int message)
    {
        if (mLinearLayoutProgress == null)
            mLinearLayoutProgress = (LinearLayout) findViewById(R.id.ll_progress);
        if (mTextViewProgress == null)
            mTextViewProgress = (TextView) findViewById(R.id.tv_progress);

        mTextViewProgress.setText(message);
        mLinearLayoutProgress.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.INVISIBLE);
        mFabPrimary.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar()
    {
        mLinearLayoutProgress.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        mFabPrimary.setVisibility(View.VISIBLE);
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
                    mFabPrimary.setVisibility(View.VISIBLE);
                    mFabPrimary.setImageResource(newDrawableId);
                    mCurrentFabIcon = newDrawableId;
                    ScaleAnimation grow = new ScaleAnimation(0f, 1.0f, 0f, 1.0f,
                            Animation.RELATIVE_TO_SELF, CENTER_PIVOT, Animation.RELATIVE_TO_SELF,
                            CENTER_PIVOT);
                    grow.setDuration(animTime);
                    grow.setInterpolator(new OvershootInterpolator());
                    mFabPrimary.startAnimation(grow);
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {
                    // does nothing
                }
            });

            mFabPrimary.startAnimation(shrink);
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
        if (mPartnerName == null)
        {
            ErrorUtil.displayErrorSnackbar(mCoordinatorLayout, R.string.text_cannot_find_pair);
            return;
        }

        message = MessageUtil.getValidMessage(message);
        if (message.startsWith(MessageUtil.MESSAGE_TYPE_ERROR))
        {
            MessageUtil.handleError(mFabPrimary, message);
            return;
        }
        final String messageText = message.substring(MessageUtil.MESSAGE_TYPE_RESERVED_LENGTH);

        final ParseObject messageObject = new ParseObject("Thought");
        messageObject.put("recipientName", mPartnerName);
        messageObject.put("senderName", mUsername);
        messageObject.put("messageText", messageText);
        messageObject.put("timeRead", 0L);

        if (MessageUtil.LOGIN_MESSAGE.equals(messageText)
                || MessageUtil.LOGOUT_MESSAGE.equals(messageText))
        {
            sendMessage(messageObject, true);
        }
        else
        {
            messageObject.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e == null)
                        sendMessage(messageObject, false);
                    else
                        messageFailedToSend(messageText);
                }
            });
        }
    }

    /**
     * Sends message to partner, using data from a {@link com.parse.ParseObject}.
     *
     * @param messageObject message data
     * @param statusMessage indicates if a status message is being sent
     */
    private void sendMessage(final ParseObject messageObject, boolean statusMessage)
    {
        JSONObject data = new JSONObject();
        try
        {
            data.put("message", messageObject.getString("messageText"));
            data.put("timestamp", MessageUtil.getCurrentDateAndTime());
            if (statusMessage)
                data.put("id", "status");
            else
                data.put("id", messageObject.getObjectId());
        }
        catch (JSONException ex)
        {
            messageFailedToSend(messageObject.getString("messageText"));
            return;
        }

        ParsePush parsePush = new ParsePush();
        parsePush.setData(data);

        ParseQuery<ParseInstallation> parseQuery = ParseInstallation.getQuery();
        parseQuery.whereEqualTo("username", mPartnerName);
        parsePush.setQuery(parseQuery);
        parsePush.sendInBackground(new SendCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                    messageFailedToSend(messageObject.getString("messageText"));
            }
        });
    }

    /**
     * Displays error message and prompts user to resend a message if it fails.
     *
     * @param messageText message that failed
     */
    private void messageFailedToSend(final String messageText)
    {
        if (MessageUtil.LOGIN_MESSAGE.equals(messageText)
                || MessageUtil.LOGOUT_MESSAGE.equals(messageText))
            return;

        Integer failureCount = mFailedMessageCount.get(messageText);
        mFailedMessageCount.put(messageText, (failureCount != null)
                ? failureCount + 1
                : 1);

        if (failureCount != null && failureCount > 2)
        {
            ErrorUtil.displayErrorSnackbar(mCoordinatorLayout, R.string.text_message_failed,
                    R.string.text_resend, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            sendMessage(messageText);
                        }
                    });
        }
        else
        {
            ErrorUtil.displayErrorSnackbar(mCoordinatorLayout, R.string.text_too_many_attemps);
        }
    }

    /**
     * Sets whether the user is currently online or not in Parse database.
     *
     * @param online user status
     */
    private void setOnlineStatus(final boolean online)
    {
        if (ParseUser.getCurrentUser() == null || !AccountUtil.doesAccountExist(this))
            return;

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String statusId = preferences.getString(MessageUtil.STATUS_OBJECT_ID, null);
        final String accountName = preferences.getString(AccountUtil.USERNAME, null);
        final String message = (online)
                ? MessageUtil.LOGIN_MESSAGE
                : MessageUtil.LOGOUT_MESSAGE;

        if (statusId == null)
        {
            final ParseObject status = new ParseObject(MessageUtil.STATUS);
            status.put(AccountUtil.USERNAME, accountName);
            status.put(MessageUtil.ONLINE_STATUS, online);
            status.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e == null)
                    {
                        mStatusAttemptCount = 0;
                        preferences.edit()
                                .putString(MessageUtil.STATUS_OBJECT_ID, status.getObjectId())
                                .apply();
                        sendMessage(message);
                    }
                    else
                    {
                        if (online)
                            statusFetchFailed();
                    }
                }
            });
        }
        else
        {
            final ParseObject status = ParseObject.createWithoutData(MessageUtil.STATUS, statusId);
            status.fetchInBackground(new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject parseObject, ParseException e)
                {
                    if (e == null)
                    {
                        mStatusAttemptCount = 0;
                        parseObject.put(MessageUtil.ONLINE_STATUS, online);
                        parseObject.saveInBackground();
                        sendMessage(message);
                    }
                    else
                    {
                        if (online)
                            statusFetchFailed();
                    }
                }
            });
        }
    }

    /**
     * Sends a message to the server to indicate the user logged in.
     */
    private void saveStatusMessage()
    {
        MessageUtil.setStatusSent(PartnerActivity.this, true);
        ParseObject status = new ParseObject("Login");
        status.put(AccountUtil.USERNAME, mUsername);
        status.saveInBackground();
    }

    /**
     * Displays error in {@link android.support.design.widget.Snackbar} when status object fails to
     * update.
     */
    private void statusFetchFailed()
    {
        if (mStatusAttemptCount < 2)
        {
            ErrorUtil.displayErrorSnackbar(mCoordinatorLayout, R.string.text_cannot_appear_online,
                    R.string.text_try_again, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            mStatusAttemptCount++;
                            setOnlineStatus(true);
                        }
                    });
        }
        else
        {
            ErrorUtil.displayErrorSnackbar(mCoordinatorLayout, R.string.text_too_many_attemps);
        }
    }

    /**
     * Checks if the user's pair is online.
     */
    private void checkIfPartnerOnline()
    {
        String partnerName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(AccountUtil.PAIR, null);
        if (partnerName == null)
            return;

        ParseQuery<ParseObject> pairStatus = new ParseQuery<>(MessageUtil.STATUS);
        pairStatus.whereEqualTo(AccountUtil.USERNAME, partnerName);
        pairStatus.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e == null && list.size() >= 0)
                {
                    boolean partnerLoggedIn = list.get(0).getBoolean(MessageUtil.ONLINE_STATUS);
                    if (partnerLoggedIn)
                    {
                        Fragment currentFragment = mPagerAdapter.getCurrentFragment();
                        if (currentFragment instanceof MessageHandler)
                            ((MessageHandler) currentFragment).onNewMessage(null,
                                    MessageUtil.getCurrentDateAndTime(), MessageUtil.LOGIN_MESSAGE);
                        if (!MessageUtil.wasStatusSent(PartnerActivity.this))
                            saveStatusMessage();
                    }
                    else
                    {
                        Fragment currentFragment = mPagerAdapter.getCurrentFragment();
                        if (currentFragment instanceof MessageHandler)
                            ((MessageHandler) currentFragment).onNewMessage(null,
                                    MessageUtil.getCurrentDateAndTime(),
                                    MessageUtil.LOGOUT_MESSAGE);
                        if (!MessageUtil.wasThoughtSent(PartnerActivity.this))
                            displayThoughPrompt();
                    }
                }
                else
                {
                    ErrorUtil.displayErrorSnackbar(mCoordinatorLayout,
                            R.string.text_cannot_find_pair);
                }
            }
        });
    }

    /**
     * Checks for status objects posted by the partner.
     */
    private void checkForPartnerLogins()
    {
        final String partnerName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(AccountUtil.PAIR, null);
        if (partnerName == null)
            return;

        ParseQuery<ParseObject> query = new ParseQuery<>("Login");
        query.whereEqualTo(AccountUtil.USERNAME, partnerName);
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e == null && list != null && list.size() > 0)
                {
                    DialogInterface.OnClickListener listener =
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if (which == DialogInterface.BUTTON_POSITIVE)
                                        showFragment(THOUGHT_FRAGMENT);
                                    dialog.dismiss();
                                }
                            };

                    String s = (list.size() > 1)
                            ? "s"
                            : "";

                    new AlertDialog.Builder(PartnerActivity.this)
                            .setTitle(R.string.text_partner_logged_in)
                            .setMessage(partnerName + " has visited " + list.size() + " time"
                                    + s + " since your last visit. Click below to see if they've "
                                    + "left you any messages.")
                            .setPositiveButton(R.string.text_dialog_thoughts, listener)
                            .setNegativeButton(R.string.text_dialog_later, listener)
                            .create()
                            .show();
                }
            }
        });
    }

    /**
     * Displays a prompt to ask the user if they want to send a thought.
     */
    private void displayThoughPrompt()
    {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                    displayThoughtDialog();
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(PartnerActivity.this)
                .setTitle(R.string.text_partner_not_online)
                .setMessage(R.string.text_send_thought)
                .setPositiveButton(R.string.text_dialog_okay, listener)
                .setNegativeButton(R.string.text_dialog_no, listener)
                .create()
                .show();
    }

    /**
     * Plays a super cute heart animation for the user.
     */
    private void superCuteHeartAnimation()
    {
        DisplayMetrics display = getResources().getDisplayMetrics();
        int deviceWidth = display.widthPixels;
        int deviceHeight = display.heightPixels;

        for (ImageView heart : mImageViewHearts)
        {
            int red = (int) (Math.random() * AnimationUtil.HEART_MAX_RED_OFFSET)
                    + AnimationUtil.HEART_DARKEST_RED;
            heart.setColorFilter(Color.rgb(red, 0, 0), PorterDuff.Mode.MULTIPLY);
            heart.startAnimation(AnimationUtil.getRandomHeartAnimation(heart,
                    (int) (Math.random() * (deviceWidth - heart.getWidth())), deviceHeight));
        }
    }

    /**
     * Animates a soft glow to fade in and out.
     */
    private void loginGlowAnimation()
    {
        final int duration = getResources().getInteger(android.R.integer.config_longAnimTime);
        final AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(duration);
        fadeIn.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                mImageViewLoginGlow.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
                fadeOut.setDuration(duration);
                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                        // does nothing
                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        mImageViewLoginGlow.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                        // does nothing
                    }
                });
                mImageViewLoginGlow.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // does nothing
            }
        });
        mImageViewLoginGlow.startAnimation(fadeIn);
    }

    /**
     * Gets the coordinator layout, for Snackbars.
     *
     * @return mCoordinatorLayout
     */
    public CoordinatorLayout getCoordinatorLayout()
    {
        return mCoordinatorLayout;
    }

    /**
     * Manages an input field for thoughts.
     */
    private final class ThoughtWatcher
            implements TextWatcher
    {

        /** Text view to display errors and text limit regarding input field. */
        private TextView mTextViewLimit;
        /** Indicates if the message is valid. */
        private boolean mIsValidMessage;

        /**
         * Assigns references to member variables.
         *
         * @param textViewLimit to display text limit and errors
         */
        private ThoughtWatcher(TextView textViewLimit)
        {
            this.mTextViewLimit = textViewLimit;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // does nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            // does nothing
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            String input = s.toString();
            if (!input.matches(MessageUtil.REGEX_VALID_MESSAGE) && mIsValidMessage)
            {
                mTextViewLimit.setText(R.string.text_message_invalid_characters);
                mTextViewLimit.setTextColor(getResources().getColor(R.color.error_color));
                mIsValidMessage = false;
                return;
            }
            else if (input.length() > MessageUtil.MAX_MESSAGE_LENGTH && mIsValidMessage)
            {
                mTextViewLimit.setTextColor(getResources().getColor(R.color.error_color));
                mIsValidMessage = false;
            }
            else if (input.length() <= MessageUtil.MAX_MESSAGE_LENGTH && !mIsValidMessage)
            {
                mTextViewLimit.setTextColor(
                        getResources().getColor(android.R.color.primary_text_dark));
                mIsValidMessage = true;
            }

            mTextViewLimit.setText(input.length() + "/" + MessageUtil.MAX_MESSAGE_LENGTH);
        }
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
}
