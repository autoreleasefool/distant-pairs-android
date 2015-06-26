package ca.josephroque.partners.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import ca.josephroque.partners.R;

/**
 * Created by Joseph Roque on 2015-06-13.
 *
 * Methods and constants for errors.
 */
public final class ErrorUtil
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "ErrorUtil";

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
    public static void displayErrorDialog(Context context, String title, String message)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.text_dialog_okay, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * Displays an error to the user in a snackbar without an action.
     *
     * @param rootView view for snackbar
     * @param message error message string id
     */
    public static void displayErrorSnackbar(View rootView, int message)
    {
        displayErrorSnackbar(rootView, message, 0, null);
    }

    /**
     * Displays an error to the user in a snackbar with an action
     *
     * @param rootView view for snackbar
     * @param message error message string id
     * @param action available action to resolve error string id
     * @param listener listener for action. If null, {@code action} will be ignored
     */
    public static void displayErrorSnackbar(View rootView, int message, int action,
                                            View.OnClickListener listener)
    {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        if (listener != null)
        {
            snackbar.setAction(action, listener);
        }

        snackbar.show();
    }

    /**
     * Displays an error to the user in a snackbar.
     *
     * @param rootView view for snackbar
     * @param message error message
     */
    public static void displayErrorSnackbar(View rootView, String message)
    {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
}
