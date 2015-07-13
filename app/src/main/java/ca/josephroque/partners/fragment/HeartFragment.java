package ca.josephroque.partners.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import ca.josephroque.partners.PartnerActivity;
import ca.josephroque.partners.R;
import ca.josephroque.partners.message.MessageHandler;
import ca.josephroque.partners.util.MessageUtils;
import ca.josephroque.partners.util.PreferenceUtils;

/**
 * A simple {@link Fragment} subclass. Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeartFragment
        extends Fragment
        implements MessageHandler
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "HeartFragment";

    /** Represents boolean indicating the partner's online status. */
    private static final String ARG_PARTNER_ONLINE = "arg_partner_online";

    /** View which display the most recent thought. */
    private View mViewMostRecentThought;
    /** Displays most recent thought received. */
    private TextView mTextViewRecentThought;
    /** Displays time that most recent thought was received. */
    private TextView mTextViewRecentThoughTime;
    /** Image of active, full heart representing an online partner. */
    private ImageView mImageViewActiveHeart;

    /** Indicates if the user's partner is online. */
    private boolean mPartnerOnline;

    /** To delay animations. */
    private Handler mHandlerPulse;
    /** Count that the pulse animation has run. */
    private int mAnimationCount;
    /** Animation which causes the heart image to pulse larger. */
    private Animation mHeartPulseGrowAnimation;
    /** Animation which causes the heart image to shrink to its original size. */
    private Animation mHeartPulseShrinkAnimation;

    /** Starts the pulse animation. */
    private Runnable mPulseAnimation = new Runnable()
    {
        @Override
        public void run()
        {
            mImageViewActiveHeart.startAnimation(mHeartPulseGrowAnimation);
        }
    };

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment HeartFragment
     */
    public static HeartFragment newInstance()
    {
        return new HeartFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_heart, container, false);

        mTextViewRecentThought = (TextView) rootView.findViewById(R.id.tv_thought_message);
        mTextViewRecentThoughTime = (TextView) rootView.findViewById(R.id.tv_thought_message_time);
        mImageViewActiveHeart = (ImageView) rootView.findViewById(R.id.iv_heart_active);

        if (savedInstanceState != null)
            mPartnerOnline = savedInstanceState.getBoolean(ARG_PARTNER_ONLINE);

        mImageViewActiveHeart.setVisibility((mPartnerOnline)
                ? View.VISIBLE
                : View.INVISIBLE);

        mViewMostRecentThought = rootView.findViewById(R.id.cv_thought);
        mViewMostRecentThought.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((PartnerActivity) getActivity()).showFragment(PartnerActivity.THOUGHT_FRAGMENT);
            }
        });

        return rootView;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        stopPulseAnimation();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_PARTNER_ONLINE, mPartnerOnline);
    }

    @Override
    public void onNewMessage(final String messageId, final String dateAndTime, final String message)
    {
        if (MessageUtils.LOGIN_MESSAGE.equals(message))
        {
            if (!mPartnerOnline)
            {
                mPartnerOnline = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    heartCircularRevealAnimation();
                else
                    heartFadeRevealAnimation();
            }
        }
        else if (MessageUtils.LOGOUT_MESSAGE.equals(message))
        {
            if (mPartnerOnline)
            {
                mPartnerOnline = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    heartCircularHideAnimation();
                else
                    heartFadeHideAnimation();
            }
        }
    }

    /**
     * Sets text for the most recent thought.
     *
     * @param message thought content
     * @param timestamp time thought was sent
     */
    public void setMostRecentThought(String message, String timestamp)
    {
        mViewMostRecentThought.setVisibility(View.VISIBLE);
        mTextViewRecentThought.setText(message);
        mTextViewRecentThoughTime.setText(timestamp);
    }

    /**
     * Circle reveals the image of the active heart.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void heartCircularRevealAnimation()
    {
        int cx = (mImageViewActiveHeart.getLeft() + mImageViewActiveHeart.getRight()) / 2;
        int cy = (mImageViewActiveHeart.getTop() + mImageViewActiveHeart.getBottom()) / 2;
        int radius = Math.max(mImageViewActiveHeart.getWidth(), mImageViewActiveHeart.getHeight());

        Animator circReveal =
                ViewAnimationUtils.createCircularReveal(mImageViewActiveHeart, cx, cy, 0, radius);
        circReveal.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        circReveal.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                startPulseAnimation();
            }
        });

        mImageViewActiveHeart.setVisibility(View.VISIBLE);
        circReveal.start();
    }

    /**
     * Circle hides the image of the active heart.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void heartCircularHideAnimation()
    {
        stopPulseAnimation();
        int cx = (mImageViewActiveHeart.getLeft() + mImageViewActiveHeart.getRight()) / 2;
        int cy = (mImageViewActiveHeart.getTop() + mImageViewActiveHeart.getBottom()) / 2;
        int radius = Math.max(mImageViewActiveHeart.getWidth(), mImageViewActiveHeart.getHeight());

        // create the animation (the final radius is zero)
        Animator circHide =
                ViewAnimationUtils.createCircularReveal(mImageViewActiveHeart, cx, cy, radius, 0);
        circHide.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));

        circHide.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mImageViewActiveHeart.setVisibility(View.GONE);
            }
        });

        circHide.start();
    }

    /**
     * Fades the image of the active heart to 1.0 alpha.
     */
    private void heartFadeRevealAnimation()
    {
        mImageViewActiveHeart.setVisibility(View.VISIBLE);
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        fade.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                // does nothing
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                startPulseAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // does nothing
            }
        });
        mImageViewActiveHeart.startAnimation(fade);
    }

    /**
     * Fades the image of the active heart to 0.0 alpha.
     */
    private void heartFadeHideAnimation()
    {
        stopPulseAnimation();
        AlphaAnimation fade = new AlphaAnimation(1f, 0f);
        fade.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        fade.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mImageViewActiveHeart.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // do nothing
            }
        });
        mImageViewActiveHeart.startAnimation(fade);
    }

    /**
     * Begins an animation which pulses the heart image.
     */
    private void startPulseAnimation()
    {
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(PreferenceUtils.PREF_ENABLE_PULSE, true))
            return;

        final float sizeToPulse = 1.04f;
        final float centerPivot = 0.5f;
        final int pulseOffset = 1000;
        final int growDuration = 80;
        final int shrinkDuration = 60;
        final int secondPulseOffset = 80;

        mHeartPulseGrowAnimation = new ScaleAnimation(1f, sizeToPulse, 1f, sizeToPulse,
                Animation.RELATIVE_TO_SELF, centerPivot, Animation.RELATIVE_TO_SELF, centerPivot);
        mHeartPulseGrowAnimation.setInterpolator(new OvershootInterpolator());
        mHeartPulseGrowAnimation.setDuration(growDuration);
        mHeartPulseGrowAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                // does nothing
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mImageViewActiveHeart.startAnimation(mHeartPulseShrinkAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // does nothing
            }
        });

        mHeartPulseShrinkAnimation = new ScaleAnimation(sizeToPulse, 1f, sizeToPulse, 1f,
                Animation.RELATIVE_TO_SELF, centerPivot, Animation.RELATIVE_TO_SELF, centerPivot);
        mHeartPulseShrinkAnimation.setInterpolator(new LinearInterpolator());
        mHeartPulseShrinkAnimation.setDuration(shrinkDuration);
        mHeartPulseShrinkAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                // does nothing
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (mAnimationCount % 2 == 0)
                    mHandlerPulse.postDelayed(mPulseAnimation, secondPulseOffset);
                else
                    mHandlerPulse.postDelayed(mPulseAnimation, pulseOffset);
                mAnimationCount++;
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // does nothing
            }
        });

        mHandlerPulse = new PulseHandler(Looper.getMainLooper());
        mHandlerPulse.postDelayed(mPulseAnimation, pulseOffset);
    }

    /**
     * Stops the pulsing heart animation.
     */
    private void stopPulseAnimation()
    {
        if (mHandlerPulse != null)
            mHandlerPulse.removeCallbacks(mPulseAnimation);
        if (mHeartPulseGrowAnimation != null)
            mHeartPulseGrowAnimation.cancel();
        if (mHeartPulseGrowAnimation != null)
            mHeartPulseShrinkAnimation.cancel();
    }

    /**
     * To delay animations.
     */
    private static final class PulseHandler
            extends Handler
    {

        /**
         * Sends {@code Looper} to super class.
         *
         * @param looper looper
         */
        private PulseHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message message)
        {
            // does nothing;
        }
    }
}
