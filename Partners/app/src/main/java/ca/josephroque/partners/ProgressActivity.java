package ca.josephroque.partners;

import android.support.v4.app.FragmentActivity;

/**
 * Provides methods for a progress bar to giving the user feedback on events taking place in the
 * application.
 */
public abstract class ProgressActivity
        extends FragmentActivity
{

    /**
     * Creates and shows a progress bar.
     *
     * @param message id of string for progress bar
     */
    public abstract void showProgressBar(int message);

    /**
     * Hides progress bar.
     */
    public abstract void hideProgressBar();
}
