package ca.josephroque.partners.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import ca.josephroque.partners.PartnerActivity;
import ca.josephroque.partners.R;
import ca.josephroque.partners.message.MessageHandler;
import ca.josephroque.partners.util.MessageUtil;

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

    private static final String ARG_PARTNER_ONLINE = "arg_partner_online";

    /** Displays most recent thought received. */
    private TextView mTextViewRecentThought;
    /** Image of active, full heart representing an online partner. */
    private ImageView mImageViewActiveHeart;

    /** Indicates if the user's partner is online. */
    private boolean mPartnerOnline;

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

        mTextViewRecentThought = (TextView) rootView.findViewById(R.id.tv_thought_most_recent);
        mImageViewActiveHeart = (ImageView) rootView.findViewById(R.id.iv_heart_active);

        if (savedInstanceState != null)
            mPartnerOnline = savedInstanceState.getBoolean(ARG_PARTNER_ONLINE);

        mImageViewActiveHeart.setVisibility((mPartnerOnline)
                ? View.VISIBLE
                : View.INVISIBLE);

        rootView.findViewById(R.id.cv_thought).setOnClickListener(new View.OnClickListener()
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
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_PARTNER_ONLINE, mPartnerOnline);
    }

    @Override
    public void onNewMessage(final String messageId, final String dateAndTime, final String message)
    {
        if (MessageUtil.LOGIN_MESSAGE.equals(message))
        {
            if (!mPartnerOnline)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    heartCircularRevealAnimation();
                else
                    heartFadeRevealAnimation();
            }
        }
        else if (MessageUtil.LOGOUT_MESSAGE.equals(message))
        {
            if (mPartnerOnline)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    heartCircularHideAnimation();
                else
                    heartFadeHideAnimation();
            }
        }
        else
        {
            mTextViewRecentThought.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mTextViewRecentThought.setText(message);
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void heartCircularRevealAnimation()
    {
        int cx = (mImageViewActiveHeart.getLeft() + mImageViewActiveHeart.getRight()) / 2;
        int cy = (mImageViewActiveHeart.getTop() + mImageViewActiveHeart.getBottom()) / 2;
        int radius = Math.max(mImageViewActiveHeart.getWidth(), mImageViewActiveHeart.getHeight());

        Animator animator =
                ViewAnimationUtils.createCircularReveal(mImageViewActiveHeart, cx, cy, 0, radius);
        mImageViewActiveHeart.setVisibility(View.VISIBLE);
        animator.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void heartCircularHideAnimation()
    {
        int cx = (mImageViewActiveHeart.getLeft() + mImageViewActiveHeart.getRight()) / 2;
        int cy = (mImageViewActiveHeart.getTop() + mImageViewActiveHeart.getBottom()) / 2;
        int radius = Math.max(mImageViewActiveHeart.getWidth(), mImageViewActiveHeart.getHeight());

        // create the animation (the final radius is zero)
        Animator animator =
                ViewAnimationUtils.createCircularReveal(mImageViewActiveHeart, cx, cy, radius, 0);

        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);
                mImageViewActiveHeart.setVisibility(View.GONE);
            }
        });

        animator.start();
    }

    private void heartFadeRevealAnimation()
    {
        mImageViewActiveHeart.setAlpha(0f);
        mImageViewActiveHeart.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
        mImageViewActiveHeart.setAnimation(animation);
    }

    private void heartFadeHideAnimation()
    {
        AlphaAnimation animation = new AlphaAnimation(1f, 0f);
        animation.setAnimationListener(new Animation.AnimationListener()
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
        mImageViewActiveHeart.startAnimation(animation);
    }
}
