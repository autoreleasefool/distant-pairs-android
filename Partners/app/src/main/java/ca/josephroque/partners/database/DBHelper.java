package ca.josephroque.partners.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    /**
     * Private constructor which creates database.
     *
     * @param context to create database
     */
    private DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
     * Drops the thoughts table and recreates it.
     *
     * @param context to get instance of database
     */
    public static void clearAllThoughts(Context context)
    {
        DBHelper helper = getInstance(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        database.execSQL("DROP TABLE IF EXISTS" + ThoughtContract.ThoughtEntry.TABLE_NAME);
        helper.createThoughtsTable(database);
    }
}
