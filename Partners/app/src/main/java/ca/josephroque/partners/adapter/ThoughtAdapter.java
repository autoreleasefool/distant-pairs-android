package ca.josephroque.partners.adapter;

import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ca.josephroque.partners.R;

/**
 * Created by Joseph Roque on 2015-06-22.
 *
 * Manages thoughts (messages) which will be displayed in a RecyclerView.
 */
public class ThoughtAdapter
        extends RecyclerView.Adapter<ThoughtAdapter.ThoughtViewHolder>
        implements View.OnClickListener
{

    /** Color of floating action button for saved thoughts. */
    private static final int COLOR_THOUGHT_SAVED = 0xffff0000;
    /** Color of floating action button for thoughts which have not been saved. */
    private static final int COLOR_THOUGHT_NOT_SAVED = 0xffffffff;

    /** Instance of callback interface. */
    private ThoughtAdapterCallback mCallback;

    /** List of unique identifiers for thoughts. */
    private List<String> mListThoughtIds;
    /** List of thoughts to display, relative to {@code mListThoughtIds}. */
    private List<String> mListThoughts;
    /** List of times, in an order relative to {@code mListThoughtIds}. */
    private List<String> mListDateAndTime;
    /** Indicates if a thought has been saved to the database. */
    private List<Boolean> mListThoughtSaved;

    /**
     * Assigns references to member variables.
     *
     * @param callback instance of callback interface
     * @param listIds list of unique identifiers
     * @param listMessages list of thoughts
     * @param listDateAndTime list of times thoughts were received
     * @param listSaved list of boolean indicating if a thought is saved
     */
    public ThoughtAdapter(ThoughtAdapterCallback callback, List<String> listIds,
                          List<String> listMessages, List<String> listDateAndTime,
                          List<Boolean> listSaved)
    {
        this.mCallback = callback;
        mListThoughtIds = listIds;
        this.mListThoughts = listMessages;
        this.mListDateAndTime = listDateAndTime;
        this.mListThoughtSaved = listSaved;
    }

    @Override
    public ThoughtViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_thought, parent, false);
        return new ThoughtViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ThoughtViewHolder viewHolder, final int position)
    {
        viewHolder.mTextViewThought.setText(mListThoughts.get(position));
        viewHolder.mTextViewTime.setText(mListDateAndTime.get(position));
        viewHolder.mFabThought.setColorFilter((mListThoughtSaved.get(position))
                ? COLOR_THOUGHT_SAVED
                : COLOR_THOUGHT_NOT_SAVED, PorterDuff.Mode.MULTIPLY);
        viewHolder.mFabThought.setTag(position);
        viewHolder.mFabThought.setOnClickListener(this);
    }

    @Override
    public int getItemCount()
    {
        return mListThoughts.size();
    }

    @Override
    public void onClick(View src)
    {
        final int position;
        try
        {
            position = (Integer) src.getTag();
        }
        catch (NullPointerException | NumberFormatException ex)
        {
            throw new IllegalStateException("thought view tag must be position");
        }

        mListThoughtSaved.set(position, !mListThoughtSaved.get(position));
        mCallback.setThoughtSavedToDatabase(position, mListThoughtIds.get(position),
                mListThoughts.get(position), mListDateAndTime.get(position),
                mListThoughtSaved.get(position));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        mCallback = null;
    }

    /**
     * Manages views for a single thought.
     */
    public class ThoughtViewHolder
            extends RecyclerView.ViewHolder
    {

        /** Floating Action Button for interaction with a thought. */
        private FloatingActionButton mFabThought;
        /** Time the thought was sent. */
        private TextView mTextViewTime;
        /** Message of a thought. */
        private TextView mTextViewThought;

        /**
         * Gets references for member variables and sends {@code itemView} to super constructor.
         *
         * @param itemView root view
         */
        public ThoughtViewHolder(View itemView)
        {
            super(itemView);
            mFabThought = (FloatingActionButton) itemView.findViewById(R.id.fab_thought);
            mTextViewTime = (TextView) itemView.findViewById(R.id.tv_thought_time);
            mTextViewThought = (TextView) itemView.findViewById(R.id.tv_thought_message);
        }
    }

    /**
     * To provide methods which the containing class must override.
     */
    public interface ThoughtAdapterCallback
    {

        /**
         * Saves or removes a thought from the database.
         *
         * @param position position of item in adapter
         * @param id unique id of thought
         * @param message thought to save or remove
         * @param time time thought was received
         * @param save if true, thought should be saved. Otherwise, it should be removed.
         */
        void setThoughtSavedToDatabase(int position, String id, String message, String time,
                                       boolean save);
    }
}
