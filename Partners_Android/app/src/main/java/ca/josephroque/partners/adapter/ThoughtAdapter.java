package ca.josephroque.partners.adapter;

import android.animation.Animator;
import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ca.josephroque.partners.R;
import ca.josephroque.partners.util.MessageUtil;

/**
 * Created by Joseph Roque on 2015-06-22.
 *
 * Manages thoughts (messages) which will be displayed in a RecyclerView.
 */
public class ThoughtAdapter
        extends RecyclerView.Adapter<ThoughtAdapter.ThoughtViewHolder>
        implements View.OnClickListener
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "ThoughtAdapter";

    /** Represents an item in the list which offers configuration settings to the user. */
    private static final int VIEWTYPE_CONFIG = 0;
    /** Represents an item in the list which displays a thought to the user. */
    private static final int VIEWTYPE_THOUGHT = 1;

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
    /** Indicates if a user has seen a thought before. */
    private List<Boolean> mListThoughtSeen;
    /** Animations attached to a view. */
    private HashMap<View, Animator> mMapViewAnimation;

    /**
     * Assigns references to member variables.
     *
     * @param callback instance of callback interface
     * @param listIds list of unique identifiers
     * @param listMessages list of thoughts
     * @param listDateAndTime list of times thoughts were received
     * @param listSaved list of boolean indicating if a thought is saved locally
     * @param listSeen list of boolean indicating if a thought has been seen
     */
    public ThoughtAdapter(ThoughtAdapterCallback callback, List<String> listIds,
                          List<String> listMessages, List<String> listDateAndTime,
                          List<Boolean> listSaved, List<Boolean> listSeen)
    {
        this.mCallback = callback;
        this.mListThoughtIds = listIds;
        this.mListThoughts = listMessages;
        this.mListDateAndTime = listDateAndTime;
        this.mListThoughtSaved = listSaved;
        this.mListThoughtSeen = listSeen;
        this.mMapViewAnimation = new HashMap<>();
    }

    @Override
    public ThoughtViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView;
        if (viewType == VIEWTYPE_THOUGHT)
            itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_thought, parent, false);
        else if (viewType == VIEWTYPE_CONFIG)
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_config, parent, false);
        else
            throw new IllegalStateException("invalid view type: " + viewType);
        return new ThoughtViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(final ThoughtViewHolder viewHolder, int initialPosition)
    {
        if (getItemViewType(initialPosition) == VIEWTYPE_CONFIG) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mCallback.openSettings();
                }
            });
            return;
        }

        // Offset to account for header
        final int position = initialPosition - 1;

        Calendar calendar = Calendar.getInstance(MessageUtil.getCurrentLocale());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date today = calendar.getTime();

        long dateAndTime = Long.parseLong(mListDateAndTime.get(position));
        Date thoughtDate = new Date(dateAndTime);

        if (thoughtDate.before(today))
            viewHolder.mTextViewTime.setText(MessageUtil.getDateFormat().format(thoughtDate));
        else
            viewHolder.mTextViewTime.setText(MessageUtil.getTimeFormat().format(thoughtDate));

        if (mMapViewAnimation.containsKey(viewHolder.itemView))
        {
            Animator animator = mMapViewAnimation.remove(viewHolder.itemView);
            animator.end();
        }
        if (mListThoughtSeen.get(position))
        {
            mListThoughtSeen.set(position, true);
            ((CardView) (viewHolder.itemView.findViewById(R.id.cl_thought_content)))
                    .setCardBackgroundColor(mCallback.getColor(R.color.thought_unread));
        }
        else
        {
            ((CardView) (viewHolder.itemView.findViewById(R.id.cl_thought_content)))
                    .setCardBackgroundColor(mCallback.getColor(R.color.primary_color_light));
        }

        if (MessageUtil.VISITED_MESSAGE.equals(mListThoughts.get(position)))
        {
            viewHolder.mTextViewThought.setText(R.string.text_partner_logged_in);
            viewHolder.mImageViewThought.setVisibility(View.GONE);
            viewHolder.mImageViewThought.setOnClickListener(null);
        }
        else
        {
            viewHolder.mTextViewThought.setText(mListThoughts.get(position));
            viewHolder.mImageViewThought.setVisibility(View.VISIBLE);
            viewHolder.mImageViewThought.setColorFilter((mListThoughtSaved.get(position))
                    ? mCallback.getColor(R.color.thought_saved)
                    : mCallback.getColor(R.color.thought_not_saved), PorterDuff.Mode.MULTIPLY);
            viewHolder.mImageViewThought.setTag(position);
            viewHolder.mImageViewThought.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount()
    {
        return mListThoughtIds.size() + 1;
    }

    @Override
    public void onClick(View src)
    {
        final int position;
        try
        {
            position = (Integer) src.getTag();
            if (position > mListThoughtIds.size())
                throw new NumberFormatException();
        }
        catch (NullPointerException | NumberFormatException ex)
        {
            throw new IllegalStateException("thought view tag must be position");
        }

        mListThoughtSaved.set(position, !mListThoughtSaved.get(position));
        mCallback.setThoughtSavedToDatabase(mListThoughtIds.get(position),
                mListThoughts.get(position), mListDateAndTime.get(position),
                mListThoughtSaved.get(position));

        // Offset to account for header
        notifyItemChanged(position + 1);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        mCallback = null;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
            return VIEWTYPE_CONFIG;
        else
            return VIEWTYPE_THOUGHT;
    }

    /**
     * Manages views for a single thought.
     */
    public class ThoughtViewHolder
            extends RecyclerView.ViewHolder
    {

        /** Image of heart to indicate whether a message is saved locally. */
        private ImageView mImageViewThought;
        /** Time the thought was sent. */
        private TextView mTextViewTime;
        /** Message of a thought. */
        private TextView mTextViewThought;

        /**
         * Gets references for member variables and sends {@code itemView} to super constructor.
         *
         * @param itemView root view
         * @param viewType type
         */
        public ThoughtViewHolder(View itemView, int viewType)
        {
            super(itemView);

            if (viewType == VIEWTYPE_CONFIG)
                return;

            mImageViewThought = (ImageView) itemView.findViewById(R.id.iv_thought_heart);
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
         * @param id unique id of thought
         * @param message thought to save or remove
         * @param time time thought was received
         * @param save if true, thought should be saved. Otherwise, it should be removed.
         */
        void setThoughtSavedToDatabase(String id, String message, String time, boolean save);

        /**
         * Requests a color from the callback interface by id.
         *
         * @param id id of color
         * @return integer value of color
         */
        int getColor(int id);

        /**
         * Invoked when the list header item is clicked.
         */
        void openSettings();
    }
}
