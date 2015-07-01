package ca.josephroque.partners.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

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
        final int duration = (int) (Math.random() * 200) + 600;
        final int translationHeight = (int) (Math.random() * (top / 3));
        final int offset = (Math.random() < 0.5)
                ? -1
                : 1;

        TranslateAnimation translate =
                new TranslateAnimation(Animation.ABSOLUTE, left, Animation.ABSOLUTE, left,
                        Animation.ABSOLUTE, top, Animation.ABSOLUTE, top - translationHeight);


        RotateAnimation rotate =
                new RotateAnimation(-45f * offset, 45f * offset, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translate);
        animationSet.addAnimation(rotate);
        animationSet.addAnimation(alpha);
        animationSet.setDuration(duration);
        animationSet.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                heart.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        return animationSet;
    }
}
