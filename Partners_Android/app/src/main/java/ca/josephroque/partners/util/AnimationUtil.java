package ca.josephroque.partners.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import ca.josephroque.partners.PartnerActivity;

/**
 * Created by Joseph Roque on 2015-06-16.
 *
 * Classes and methods for creating animations.
 */
public final class AnimationUtil
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "AnimationUtil";

    /**
     * For every X pixels, 10 hearts are added to the super cute heart animation.
     *
     * @see PartnerActivity#superCuteHeartAnimation()
     */
    public static final float HEART_RATIO = 1400;

    /**
     * Ratio of hearts which will be larger vs smaller in the super cute heart animation.
     *
     * @see PartnerActivity#superCuteHeartAnimation()
     */
    public static final int HEART_SIZE_RATIO = 3;

    /**
     * Minimum red value for RGB of hearts in super cute heart animation.
     *
     * @see PartnerActivity#superCuteHeartAnimation()
     */
    public static final int HEART_DARKEST_RED = 155;

    /**
     * Maximum value to add to {@code HEART_DARKEST_RED} for RGB of hearts in super cute heart
     * animation.
     *
     * @see PartnerActivity#superCuteHeartAnimation()
     */
    public static final int HEART_MAX_RED_OFFSET = 65;

    /**
     * Default private constructor.
     */
    private AnimationUtil()
    {
        // does nothing
    }

    /**
     * Creates a random animation set for a view, moving upwards, rotating back and forth, and
     * fading.
     *
     * @param heart view for animation
     * @param left starting left position of view
     * @param top starting top position of view
     * @return animation for adorable hearts
     */
    public static AnimationSet getRandomHeartAnimation(final View heart, int left, int top)
    {
        final int duration = (int) (Math.random() * 1000) + 1000;
        final int translationHeight = (int) (Math.random() * (top / 6)) + (top / 4);

        TranslateAnimation translate =
                new TranslateAnimation(Animation.ABSOLUTE, left, Animation.ABSOLUTE, left,
                        Animation.ABSOLUTE, top, Animation.ABSOLUTE, top - translationHeight);
        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translate);
        animationSet.addAnimation(alpha);
        animationSet.setDuration(duration);
        animationSet.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                heart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                heart.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // does nothing
            }
        });
        return animationSet;
    }
}
