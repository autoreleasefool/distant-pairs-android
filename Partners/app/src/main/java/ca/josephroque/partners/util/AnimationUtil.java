package ca.josephroque.partners.util;

import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

/**
 * Created by Joseph Roque on 2015-06-16.
 *
 * Classes and methods for creating animations.
 */
public final class AnimationUtil
{

    /**
     * Default private constructor.
     */
    private AnimationUtil()
    {
        // does nothing
    }

    /**
     * Creates a random animation set for a view, moving upwards, rotating back and forth,
     * and fading.
     *
     * @param left starting left position of view
     * @param top  starting top position of view
     * @return animation for adorable hearts
     */
    public static AnimationSet getRandomHeartAnimation(int left, int top)
    {
        AnimationSet animationSet = new AnimationSet(true);
        return animationSet;
    }
}
