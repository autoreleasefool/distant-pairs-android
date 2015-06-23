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

import java.util.ArrayList;
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

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
    public void onNewMessage(String dateAndTime, String message)
    {
        // TODO: display new thought
    }

    @Override
    public void setThoughtSavedToDatabase(int position, String id, String message, String time,
                                          boolean save)
    {
        mRecyclerViewThoughtsAdapter.notifyItemChanged(position);
    }

    /**
     * Loads messages from the internal database and Parse database and adds them to the recycler
     * view.
     */
    private final class PopulateMessagesTask
            extends AsyncTask<Void, Void, Integer>
    {

        @Override
        protected Integer doInBackground(Void... params)
        {
            if (getActivity() == null)
                return ParseException.USERNAME_MISSING;

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

            // TODO: check server for messages

            for (Map.Entry<String, Pair<String, String>> entry : thoughtMap.entrySet())
            {
                mListDateAndTime.add(entry.getKey());
                mListThoughtIds.add(entry.getValue().first);
                mListThoughts.add(entry.getValue().second);
                mListThoughtSaved.add(savedMap.get(entry.getKey()));
            }
            return AccountUtil.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            switch (result)
            {
                case AccountUtil.SUCCESS:
                    mRecyclerViewThoughtsAdapter.notifyDataSetChanged();
                default:
                    // does nothing
            }
        }
    }
}
