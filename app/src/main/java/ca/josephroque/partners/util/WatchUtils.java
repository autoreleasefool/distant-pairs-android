package ca.josephroque.partners.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import ca.josephroque.partners.R;

/**
 * Created by Joseph Roque on 2015-07-09. Methods for adding and removing a watch from the user's
 * account.
 */
public final class WatchUtils
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "WatchUtils";

    /** Represents the id of the watch associated with the account. */
    public static final String WATCH_ID = "watch_id";

    /**
     * Prompts user to add a watch to their account.
     *
     * @param context to create dialog
     */
    public static void addWatch(final Context context)
    {
        if (isWatchSet(context))
            return;

        final EditText editText = new EditText(context);
        LinearLayout.LayoutParams layoutParams
                = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(layoutParams);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                    addWatch(context, editText.getText().toString());
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(context)
                .setTitle(R.string.text_add_watch_title)
                .setMessage(R.string.text_add_watch_message)
                .setView(editText)
                .setPositiveButton(R.string.text_dialog_add, listener)
                .setNegativeButton(R.string.text_dialog_cancel, listener)
                .create()
                .show();
    }

    /**
     * Gets a watch parse object from an id and adds the user's account name to it.
     *
     * @param context to get preferences
     * @param watchId id of the parse object
     */
    private static void addWatch(final Context context, final String watchId)
    {
        final SharedPreferences preferences
                = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(WATCH_ID, "").apply();

        ParseObject watchObject = ParseObject.createWithoutData("Watch", watchId);
        watchObject.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if (e == null && parseObject != null)
                {
                    parseObject.put(AccountUtils.USERNAME,
                            preferences.getString(AccountUtils.USERNAME, null));
                    parseObject.put(AccountUtils.PAIR,
                            preferences.getString(AccountUtils.PAIR, null));
                    parseObject.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e != null)
                                preferences.edit().putString(WATCH_ID, watchId).apply();
                            else
                            {
                                preferences.edit().remove(WATCH_ID).apply();
                                ErrorUtils.displayErrorDialog(context,
                                        context.getResources()
                                                .getString(R.string.text_error_adding_watch),
                                        context.getResources()
                                                .getString(R.string.text_invalid_watch));
                            }
                        }
                    });
                }
                else
                {
                    preferences.edit().remove(WATCH_ID).apply();
                    ErrorUtils.displayErrorDialog(context,
                            context.getResources().getString(R.string.text_error_adding_watch),
                            context.getResources().getString(R.string.text_invalid_watch));
                }
            }
        });

    }

    /**
     * Prompts user to delete a watch from the user's account.
     *
     * @param context to create dialog
     */
    public static void deleteWatch(final Context context)
    {
        if (!isWatchSet(context))
            return;

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                            context);
                    String watchId = preferences.getString(WATCH_ID, null);

                    if (watchId != null)
                    {
                        ParseObject watchObject = ParseObject.createWithoutData("Watch", watchId);
                        watchObject.deleteInBackground();
                        preferences.edit().remove(WATCH_ID).apply();
                    }
                }
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(context)
                .setTitle(R.string.text_delete_watch_title)
                .setMessage(R.string.text_delete_watch_message)
                .setPositiveButton(R.string.text_dialog_delete, listener)
                .setNegativeButton(R.string.text_dialog_cancel, listener)
                .create()
                .show();
    }

    /**
     * Checks if a watch has been set in the preferences.
     *
     * @param context to get shared preferences
     * @return true if the user has set a watch, false otherwise
     */
    public static boolean isWatchSet(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(WATCH_ID, null)
                != null;
    }

    /**
     * Default private constructor.
     */
    private WatchUtils()
    {
        // does nothing
    }
}
