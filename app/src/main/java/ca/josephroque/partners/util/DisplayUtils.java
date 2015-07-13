package ca.josephroque.partners.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Joseph Roque on 2015-06-24. Constants and methods for changes made to the UI.
 */
public final class DisplayUtils
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "DisplayUtils";

    /**
     * Default private constructor.
     */
    private DisplayUtils()
    {
        // does nothing
    }

    /**
     * Offset for converting dp to px.
     */
    private static final float DENSITY_OFFSET = 0.5f;

    /**
     * Converts a dp value to pixels according to display density.
     *
     * @param context to get display density
     * @param dp value to convert
     * @return {@code dp} in pixels
     */
    public static int convertDpToPx(Context context, float dp)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + DENSITY_OFFSET);
    }

    /**
     * Hides the keyboard in the current activity.
     *
     * @param activity current activity
     */
    public static void hideKeyboard(Activity activity)
    {
        View view = activity.getCurrentFocus();
        if (view != null)
        {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
