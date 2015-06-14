package ca.josephroque.partners.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Joseph Roque on 2015-06-13.
 * <p/>
 * Methods and constants for errors.
 */
public final class ErrorUtil
{

    /**
     * Default private constructor.
     */
    private ErrorUtil()
    {
        // does nothing
    }

    /**
     * Displays an error prompt to the user.
     *
     * @param context to display message
     * @param title title of error
     * @param message error message
     */
    public static void displayErrorMessage(Context context, String title, String message)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
