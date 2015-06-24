package ca.josephroque.partners.util;

import android.content.Context;

/**
 * Created by Joseph Roque on 2015-06-24.
 *
 * Constants and methods for changes made to the UI.
 */
public final class DisplayUtil
{

    /**
     * Default private constructor.
     */
    private DisplayUtil()
    {
        // does nothing
    }

    /**
     * Converts a dp value to pixels according to display's density
     *
     * @param context to get display density
     * @param dp value to convert
     * @return {@code dp} in pixels
     */
    public static int convertDpToPx(Context context, float dp)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }
}
