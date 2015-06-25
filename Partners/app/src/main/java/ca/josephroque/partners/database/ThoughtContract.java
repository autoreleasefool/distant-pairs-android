package ca.josephroque.partners.database;

import android.provider.BaseColumns;

/**
 * Created by Joseph Roque on 2015-06-23.
 *
 * Constants to define tables in database.
 */
public final class ThoughtContract
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "ThoughtContract";

    /**
     * Default private constructor.
     */
    private ThoughtContract()
    {
        // does nothing
    }

    /**
     * Thought table definition.
     */
    public static final class ThoughtEntry
            implements BaseColumns
    {

        /**
         * Default private constructor.
         */
        private ThoughtEntry()
        {
            // does nothing
        }

        /** Name of thought table. */
        public static final String TABLE_NAME = "thoughts";
        /** Column representing unique thought id. */
        public static final String COLUMN_ID = "thought_id";
        /** Column representing thought message. */
        public static final String COLUMN_MESSAGE = "thought_message";
        /** Column representing thought date and time. */
        public static final String COLUMN_TIME = "thought_time";
    }
}
