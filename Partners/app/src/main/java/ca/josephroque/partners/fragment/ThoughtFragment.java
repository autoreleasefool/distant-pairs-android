package ca.josephroque.partners.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.josephroque.partners.R;
import ca.josephroque.partners.adapter.ThoughtAdapter;
import ca.josephroque.partners.database.DBHelper;
import ca.josephroque.partners.database.ThoughtContract;
import ca.josephroque.partners.interfaces.MessageHandler;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;
import ca.josephroque.partners.util.MessageUtil;

/**
 * A simple {@link Fragment} subclass. Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThoughtFragment
        extends Fragment
        implements MessageHandler, ThoughtAdapter.ThoughtAdapterCallback
{

    /** To identify output from this class in Logcat. */
    private static final String TAG = "ThoughtFrag";

    /** Manages data in a {@link RecyclerView}. */
    private ThoughtAdapter mRecyclerViewThoughtsAdapter;

    /** List of unique identifiers for thoughts. */
    private List<String> mListThoughtIds;
    /** List of thoughts to display, relative to {@code mListThoughtIds}. */
    private List<String> mListThoughts;
    /** List of times, in an order relative to {@code mListThoughtIds}. */
    private List<String> mListDateAndTime;
    /** Indicates if a thought has been saved to the database. */
    private List<Boolean> mListThoughtSaved;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment ThoughtFragment.
     */
    public static ThoughtFragment newInstance()
    {
        return new ThoughtFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_thought, container, false);

        // Initialize lists
        mListThoughtIds = new ArrayList<>();
        mListThoughts = new ArrayList<>();
        mListDateAndTime = new ArrayList<>();
        mListThoughtSaved = new ArrayList<>();

        mRecyclerViewThoughtsAdapter = new ThoughtAdapter(this, mListThoughtIds, mListThoughts,
                mListDateAndTime, mListThoughtSaved);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // TODO: check if below works
        // if not, try: layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_thoughts);
        recyclerView.setAdapter(mRecyclerViewThoughtsAdapter);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        new PopulateMessagesTask().execute();
    }

    @Override
    public void onNewMessage(final String messageId, final String dateAndTime, final String message)
    {
        // Does not need to bother with login/out messages
        if (MessageUtil.LOGIN_MESSAGE.equals(message)
                || MessageUtil.LOGOUT_MESSAGE.equals(message))
            // TODO: possibly display animation on login/logout
            return;

        mListThoughtIds.add(0, messageId);
        mListDateAndTime.add(0, dateAndTime);
        mListThoughts.add(0, message);
        mListThoughtSaved.add(0, false);
        mRecyclerViewThoughtsAdapter.notifyItemInserted(0);
        // TODO: show heart animation
    }

    @Override
    public void setThoughtSavedToDatabase(int position, String id, String message, String time,
                                          boolean save)
    {
        mRecyclerViewThoughtsAdapter.notifyItemChanged(position);
        // TODO: save thought to database, remove from parse
    }

    /**
     * Loads messages from the internal database and Parse database and adds them to the recycler
     * view.
     */
    private final class PopulateMessagesTask
            extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params)
        {
            if (getActivity() == null)
                return null;

            final SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String username = preferences.getString(AccountUtil.USERNAME, null);
            final String partnerName = preferences.getString(AccountUtil.PAIR, null);

            if (username == null || partnerName == null)
                throw new IllegalStateException("name or partner cannot be null");

            // To order thoughts by time.
            // Key is date/time, value is pair containing id and message
            TreeMap<String, Pair<String, String>> thoughtMap = new TreeMap<>();

            // To indicate if a thought was retrieved from the database or not
            HashMap<String, Boolean> savedMap = new HashMap<>();

            getDatabaseThoughts(thoughtMap, savedMap);
            getParseThoughts(preferences, thoughtMap, savedMap);

            for (Map.Entry<String, Pair<String, String>> entry : thoughtMap.entrySet())
            {
                mListDateAndTime.add(entry.getKey());
                mListThoughtIds.add(entry.getValue().first);
                mListThoughts.add(entry.getValue().second);
                mListThoughtSaved.add(savedMap.get(entry.getKey()));
            }
            return null;
        }

        /**
         * Gets thoughts stored in internal database and populates maps.
         *
         * @param thoughtMap thought contents
         * @param savedMap values will be true for any messages loaded
         */
        private void getDatabaseThoughts(TreeMap<String, Pair<String, String>> thoughtMap,
                                         HashMap<String, Boolean> savedMap)
        {
            String rawThoughtQuery = "SELECT "
                    + ThoughtContract.ThoughtEntry.COLUMN_ID + ", "
                    + ThoughtContract.ThoughtEntry.COLUMN_MESSAGE + ", "
                    + ThoughtContract.ThoughtEntry.COLUMN_TIME
                    + " FROM " + ThoughtContract.ThoughtEntry.TABLE_NAME
                    + " ORDER BY " + ThoughtContract.ThoughtEntry.COLUMN_TIME + " DESC";
            SQLiteDatabase database = DBHelper.getInstance(getActivity()).getReadableDatabase();
            Cursor cursor = database.rawQuery(rawThoughtQuery, null);

            try
            {
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        String date = cursor.getString(cursor.getColumnIndex(
                                ThoughtContract.ThoughtEntry.COLUMN_TIME));
                        String id = cursor.getString(cursor.getColumnIndex(
                                ThoughtContract.ThoughtEntry.COLUMN_ID));
                        String message = cursor.getString(cursor.getColumnIndex(
                                ThoughtContract.ThoughtEntry.COLUMN_MESSAGE));

                        thoughtMap.put(date, Pair.create(id, message));
                        savedMap.put(date, true);
                        cursor.moveToNext();
                    }
                }
            }
            catch (Exception ex)
            {
                Log.e(TAG, "Error reading thoughts", ex);
            }
            finally
            {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            }
        }

        /**
         * Gets thoughts stored in pARSE database and populates maps.
         *
         * @param preferences to get user object id
         * @param thoughtMap thought contents
         * @param savedMap values will be false for any messages loaded
         */
        private void getParseThoughts(SharedPreferences preferences,
                                      TreeMap<String, Pair<String, String>> thoughtMap,
                                      HashMap<String, Boolean> savedMap)
        {
            final String userParseId = preferences.getString(AccountUtil.PARSE_USER_ID, null);
            if (userParseId != null)
            {
                ParseQuery<ParseObject> thoughtQuery = new ParseQuery<>("Thought");
                thoughtQuery.whereEqualTo("recipientId", userParseId);
                List<ParseObject> thoughtResults = Collections.emptyList();

                try
                {
                    thoughtResults = thoughtQuery.find();
                }
                catch (ParseException ex)
                {
                    // does nothing - no thoughts found, or no connection
                }

                for (ParseObject thought : thoughtResults)
                {
                    String date = thought.getString("sentTime");
                    String id = thought.getString("sinchId");
                    String message = thought.getString("messageText");

                    thoughtMap.put(date, Pair.create(id, message));
                    savedMap.put(date, false);
                }
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (mListThoughtIds.size() == 0)
            {
                ErrorUtil.displayErrorMessage(getActivity(), "No thoughts loaded",
                        "Could not find any thoughts from your pair to display.");
            }
            else
            {
                mRecyclerViewThoughtsAdapter.notifyDataSetChanged();
            }
        }
    }
}
