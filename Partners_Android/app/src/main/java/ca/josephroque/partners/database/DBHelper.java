package ca.josephroque.partners.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.ref.WeakReference;
import java.util.List;

import ca.josephroque.partners.R;
import ca.josephroque.partners.util.ErrorUtil;

/**
 * Created by Joseph Roque on 2015-06-23.
 *
 * Database helper to access internal database.
 */
public final class DBHelper
        extends SQLiteOpenHelper
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "DBHelper";

    /** Name of database. */
    private static final String DATABASE_NAME = "thought_db";
    /** Version of database. */
    private static final int DATABASE_VERSION = 1;

    /** Singleton instance of {@code DBHelper}. */
    private static DBHelper sDatabaseHelperInstance;

    /** Weak reference to context which created instance. */
    private WeakReference<Context> mContext;

    /**
     * Private constructor which creates database.
     *
     * @param context to create database
     */
    private DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = new WeakReference<>(context);
    }

    /**
     * Returns singleton instance of this class.
     *
     * @param context to create database
     * @return {@code sDatabaseHelperInstance}
     */
    public static DBHelper getInstance(Context context)
    {
        if (sDatabaseHelperInstance == null)
        {
            sDatabaseHelperInstance = new DBHelper(context);
        }

        return sDatabaseHelperInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        createThoughtsTable(db);
    }

    /**
     * Creates the thoughts table in the database. See
     * {@link ca.josephroque.partners.database.ThoughtContract.ThoughtEntry} for table definition.
     *
     * @param db database
     */
    private void createThoughtsTable(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "
                + ThoughtContract.ThoughtEntry.TABLE_NAME + " ("
                + ThoughtContract.ThoughtEntry._ID + " INTEGER PRIMARY KEY, "
                + ThoughtContract.ThoughtEntry.COLUMN_ID + " TEXT NOT NULL UNIQUE, "
                + ThoughtContract.ThoughtEntry.COLUMN_MESSAGE + " TEXT NOT NULL, "
                + ThoughtContract.ThoughtEntry.COLUMN_TIME + " TEXT NOT NULL"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + ThoughtContract.ThoughtEntry.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Saves a thought to the thoughts table in the database, removes from Parse database.
     *
     * @param id unique id of message
     * @param message thought
     * @param time time message was sent
     */
    public void saveThoughtToDatabase(final String id, final String message, final String time)
    {
        SQLiteDatabase database = getWritableDatabase();
        try
        {
            database.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(ThoughtContract.ThoughtEntry.COLUMN_ID, id);
            contentValues.put(ThoughtContract.ThoughtEntry.COLUMN_MESSAGE, message);
            contentValues.put(ThoughtContract.ThoughtEntry.COLUMN_TIME, time);
            database.insert(ThoughtContract.ThoughtEntry.TABLE_NAME, null, contentValues);

            database.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            if (mContext.get() != null)
                ErrorUtil.displayErrorDialog(mContext.get(), "Database error",
                        "Unfortunately, your message could not be saved locally");
            return;
        }
        finally
        {
            database.endTransaction();
        }

        ParseObject parseObject = ParseObject.createWithoutData("Thought", id);
        parseObject.deleteInBackground();
    }

    /**
     * Prompts user to delete a thought from the database, and removes it if they accept.
     *
     * @param activity to create dialog
     * @param id unique id of thought to delete
     * @param message thought
     * @param callback to notify of deleted thought.
     */
    public void promptDeleteThoughtFromDatabase(final Activity activity,
                                                final String id,
                                                final String message,
                                                final DBDeleteCallback callback)
    {
        final View rootView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_delete_thought, null);
        ((TextView) rootView.findViewById(R.id.tv_thought_to_delete)).setText(message);
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    callback.thoughtDeleted(id);
                    deleteThoughtFromDatabase(id);
                }
                dialog.dismiss();
            }
        };

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                new AlertDialog.Builder(activity)
                        .setView(rootView)
                        .setPositiveButton(R.string.text_dialog_okay, listener)
                        .setNegativeButton(R.string.text_dialog_cancel, listener)
                        .create()
                        .show();
            }
        });
    }

    /**
     * Deletes a thought from the database.
     *
     * @param id unique id of thought to delete
     */
    public void deleteThoughtFromDatabase(final String id)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(ThoughtContract.ThoughtEntry.TABLE_NAME,
                ThoughtContract.ThoughtEntry.COLUMN_ID + "=?",
                new String[]{id});
    }

    /**
     * Drops the thoughts table and recreates it.
     */
    public void clearAllThoughts()
    {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + ThoughtContract.ThoughtEntry.TABLE_NAME);
        createThoughtsTable(database);
    }

    /**
     * Callback interface for when a thought is deleted from the local database.
     */
    public interface DBDeleteCallback
    {
        /**
         * Indicates a thought was deleted from the database.
         *
         * @param id id of deleted thought.
         */
        void thoughtDeleted(String id);
    }
}
