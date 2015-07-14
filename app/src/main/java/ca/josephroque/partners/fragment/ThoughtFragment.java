package ca.josephroque.partners.fragment;

import android.app.Activity;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.josephroque.partners.PartnerActivity;
import ca.josephroque.partners.R;
import ca.josephroque.partners.SettingsActivity;
import ca.josephroque.partners.adapter.ThoughtAdapter;
import ca.josephroque.partners.database.DBHelper;
import ca.josephroque.partners.database.ThoughtContract;
import ca.josephroque.partners.message.MessageHandler;
import ca.josephroque.partners.util.AccountUtils;
import ca.josephroque.partners.util.ErrorUtils;
import ca.josephroque.partners.util.MessageUtils;

/**
 * A simple {@link Fragment} subclass. Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThoughtFragment
        extends Fragment
        implements MessageHandler, ThoughtAdapter.ThoughtAdapterCallback
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "ThoughtFragment";

    /** Manages data in a {@link RecyclerView}. */
    private ThoughtAdapter mRecyclerViewThoughtsAdapter;

    /** Executes database operations in order. */
    private final ExecutorService mDatabaseExecutorService = Executors.newSingleThreadExecutor();

    /** Instance of callback interface. */
    private ThoughtFragmentCallbacks mCallback;

    /** List of unique identifiers for thoughts. */
    private List<String> mListThoughtIds;
    /** List of thoughts to display, relative to {@code mListThoughtIds}. */
    private List<String> mListThoughts;
    /** List of times, in an order relative to {@code mListThoughtIds}. */
    private List<String> mListDateAndTime;
    /** Indicates if a thought has been saved to the database. */
    private List<Boolean> mListThoughtSaved;
    /** Indicates if a user has seen a thought before. */
    private List<Boolean> mListThoughtSeen;

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
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            mCallback = (ThoughtFragmentCallbacks) activity;
        }
        catch (ClassCastException ex)
        {
            throw new ClassCastException("activity must implement callback interface "
                    + "ThoughtCallbackFragment");
        }
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
        mListThoughtSeen = new ArrayList<>();

        MessageUtils.setLocale(getResources().getConfiguration().locale);
        mRecyclerViewThoughtsAdapter = new ThoughtAdapter(this, mListThoughtIds, mListThoughts,
                mListDateAndTime, mListThoughtSaved, mListThoughtSeen);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_thoughts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mRecyclerViewThoughtsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (getActivity() == null)
            return;

        MessageUtils.setLocale(getResources().getConfiguration().locale);
        new PopulateMessagesTask().execute();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mCallback = null;
    }

    @Override
    public void onNewMessage(final String messageId, final String dateAndTime, final String message)
    {
        // Does not need to bother with login/out messages
        if (MessageUtils.LOGIN_MESSAGE.equals(message)
                || MessageUtils.LOGOUT_MESSAGE.equals(message)
                || MessageUtils.VISITED_MESSAGE.equals(message))
            return;

        mListThoughtIds.add(0, messageId);
        mListDateAndTime.add(0, dateAndTime);
        mListThoughts.add(0, message);
        mListThoughtSaved.add(0, false);
        mListThoughtSeen.add(0, false);

        // Offset by +1 to account for header in adapter
        mRecyclerViewThoughtsAdapter.notifyItemInserted(1);

        Calendar calendar = Calendar.getInstance(MessageUtils.getCurrentLocale());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date today = calendar.getTime();

        long thoughtTime = Long.parseLong(dateAndTime);
        Date thoughtDate = new Date(thoughtTime);

        if (mCallback != null)
        {
            if (thoughtDate.before(today))
                mCallback.setMostRecentThought(message,
                        MessageUtils.getDateFormat().format(thoughtDate));
            else
                mCallback.setMostRecentThought(message,
                        MessageUtils.getTimeFormat().format(thoughtDate));
        }
    }

    @Override
    public int getColor(int colorId)
    {
        return getResources().getColor(colorId);
    }

    @Override
    public void setThoughtSavedToDatabase(final String id, final String message, final String time,
                                          final boolean save,
                                          final ThoughtAdapter.ThoughtDatabaseCallback callback)
    {
        mDatabaseExecutorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                DBHelper helper = DBHelper.getInstance(getActivity());
                if (save)
                {
                    helper.saveThoughtToDatabase(id, message, time);
                    callback.onThoughtSavedToDatabase();
                }
                else
                {
                    helper.promptDeleteThoughtFromDatabase(getActivity(), id, message,
                            new DBHelper.DBDeleteCallback()
                            {
                                @Override
                                public void thoughtDeleted(String id)
                                {
                                    final int indexToDelete = mListThoughtIds.indexOf(id);
                                    if (indexToDelete > -1)
                                    {
                                        mListThoughtIds.remove(indexToDelete);
                                        mListThoughts.remove(indexToDelete);
                                        mListDateAndTime.remove(indexToDelete);
                                        mListThoughtSaved.remove(indexToDelete);
                                        callback.onThoughtDeletedFromDatabase();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public void openSettings()
    {
        Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
        getActivity().startActivity(settingsIntent);
    }

    /**
     * Loads messages from the internal database and Parse database and adds them to the recycler
     * view.
     */
    private final class PopulateMessagesTask
            extends AsyncTask<Void, Void, Void>
    {

        /**
         * Indicates if a message with the content {@link
         * ca.josephroque.partners.util.MessageUtils#VISITED_MESSAGE} was found.
         */
        private boolean mVisitedMessageFound = false;

        @Override
        protected Void doInBackground(Void... params)
        {
            if (getActivity() == null)
                return null;

            final SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String username = preferences.getString(AccountUtils.USERNAME, null);
            final String partnerName = preferences.getString(AccountUtils.PAIR, null);

            if (username == null || partnerName == null)
                throw new IllegalStateException("name or partner cannot be null");

            // To order thoughts by time.
            // Key is date/time, value is pair containing id and message
            TreeMap<String, Pair<String, String>> thoughtMap
                    = new TreeMap<>(Collections.reverseOrder());

            // To indicate if a thought was retrieved from the database or not, and if it has
            // been seen before
            HashMap<String, Pair<Boolean, Boolean>> savedSeenMap = new HashMap<>();

            getDatabaseThoughts(thoughtMap, savedSeenMap);
            getParseThoughts(preferences, thoughtMap, savedSeenMap);

            mListThoughts.clear();
            mListThoughtIds.clear();
            mListThoughts.clear();
            mListThoughtSaved.clear();
            mListThoughtSeen.clear();
            for (Map.Entry<String, Pair<String, String>> entry : thoughtMap.entrySet())
            {
                mListDateAndTime.add(entry.getKey());
                mListThoughtIds.add(entry.getValue().first);
                mListThoughts.add(entry.getValue().second);
                mListThoughtSaved.add(savedSeenMap.get(entry.getKey()).first);
                mListThoughtSeen.add(savedSeenMap.get(entry.getKey()).second);
            }
            return null;
        }

        /**
         * Gets thoughts stored in internal database and populates maps.
         *
         * @param thoughtMap thought contents
         * @param savedSeenMap values will be true for any messages loaded
         */
        private void getDatabaseThoughts(TreeMap<String, Pair<String, String>> thoughtMap,
                                         HashMap<String, Pair<Boolean, Boolean>> savedSeenMap)
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
                        savedSeenMap.put(date, Pair.create(true, true));
                        cursor.moveToNext();
                    }
                }
            }
            catch (Exception ex)
            {
                // does nothing
            }
            finally
            {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            }
        }

        /**
         * Gets thoughts stored in Parse database and populates maps.
         *
         * @param preferences to get user object id
         * @param thoughtMap thought contents
         * @param savedSeenMap values will be false for any messages loaded
         */
        private void getParseThoughts(SharedPreferences preferences,
                                      TreeMap<String, Pair<String, String>> thoughtMap,
                                      HashMap<String, Pair<Boolean, Boolean>> savedSeenMap)
        {
            final String username = preferences.getString(AccountUtils.USERNAME, null);
            final String partnerName = preferences.getString(AccountUtils.PAIR, null);
            if (username != null && partnerName != null)
            {
                ParseQuery<ParseObject> thoughtQuery = new ParseQuery<>("Thought")
                        .whereEqualTo("recipientName", username)
                        .whereEqualTo("senderName", partnerName);
                List<ParseObject> thoughtResults = Collections.emptyList();

                try
                {
                    thoughtResults = thoughtQuery.find();
                }
                catch (ParseException ex)
                {
                    // does nothing - no thoughts found, or no connection
                }

                if (thoughtResults == null)
                    thoughtResults = Collections.emptyList();

                List<ParseObject> thoughtsToSave = new ArrayList<>(thoughtResults.size());
                List<ParseObject> thoughtsToDelete = new ArrayList<>(thoughtResults.size());

                for (ParseObject thought : thoughtResults)
                {
                    String date = Long.toString(thought.getCreatedAt().getTime());
                    String id = thought.getObjectId();
                    String message = thought.getString("messageText");

                    thoughtMap.put(date, Pair.create(id, message));

                    if (MessageUtils.VISITED_MESSAGE.equals(message))
                    {
                        thoughtsToDelete.add(thought);
                        mVisitedMessageFound = true;
                        savedSeenMap.put(date, Pair.create(false, false));
                    }
                    else if (thought.getLong("timeRead") == 0)
                    {
                        thought.put("timeRead", new Date().getTime());
                        thoughtsToSave.add(thought);
                        savedSeenMap.put(date, Pair.create(false, false));
                    }
                    else
                    {
                        savedSeenMap.put(date, Pair.create(false, true));
                    }
                }

                ParseObject.saveAllInBackground(thoughtsToSave);
                ParseObject.deleteAllInBackground(thoughtsToDelete);
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (mListThoughtIds.size() == 0)
            {
                ErrorUtils.displayErrorSnackbar(
                        ((PartnerActivity) getActivity()).getCoordinatorLayout(),
                        R.string.text_no_thoughts);
                if (mCallback != null)
                    mCallback.setMostRecentThought(
                            getResources().getString(R.string.text_no_thoughts), null);
            }
            else
            {
                mRecyclerViewThoughtsAdapter.notifyDataSetChanged();
                Calendar calendar = Calendar.getInstance(MessageUtils.getCurrentLocale());
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date today = calendar.getTime();

                long thoughtTime = Long.parseLong(mListDateAndTime.get(0));
                Date thoughtDate = new Date(thoughtTime);

                int firstMessage = 0;
                while (firstMessage < mListThoughts.size() &&
                        MessageUtils.VISITED_MESSAGE.equals(mListThoughts.get(firstMessage)))
                    firstMessage++;

                if (mCallback != null)
                {
                    if (firstMessage < mListThoughts.size()) {
                        if (thoughtDate.before(today))
                            mCallback.setMostRecentThought(mListThoughts.get(firstMessage),
                                    MessageUtils.getDateFormat().format(thoughtDate));
                        else
                            mCallback.setMostRecentThought(mListThoughts.get(firstMessage),
                                    MessageUtils.getTimeFormat().format(thoughtDate));
                    }
                    if (mVisitedMessageFound)
                        mCallback.notifyOfLogins();
                }
            }
        }
    }

    /**
     * Provides callback utilities from activity.
     */
    public interface ThoughtFragmentCallbacks
    {

        /**
         * Should update the most recent thought wherever necessary.
         *
         * @param message thought contents
         * @param timestamp time thought was sent
         */
        void setMostRecentThought(String message, String timestamp);

        /**
         * Invoked if a message with the content {@link
         * ca.josephroque.partners.util.MessageUtils#VISITED_MESSAGE} is found.
         */
        void notifyOfLogins();
    }
}
