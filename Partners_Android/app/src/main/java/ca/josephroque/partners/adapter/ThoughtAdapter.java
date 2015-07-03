package ca.josephroque.partners.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "ThoughtAdapter";

    /** Instance of callback interface. */
    private ThoughtAdapterCallback mCallback;

    /** Formats a date to display in the current timezone. */
    private DateFormat mDateFormat;
    /** Formats a date to display as the time only in the current timezone. */
    private DateFormat mTimeFormat;
    /** The current locale. */
    private Locale mCurrentLocale;

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
     * @param locale current locale
     */
    public ThoughtAdapter(ThoughtAdapterCallback callback, List<String> listIds,
                          List<String> listMessages, List<String> listDateAndTime,
                          List<Boolean> listSaved, List<Boolean> listSeen, Locale locale)
    {
        this.mCallback = callback;
        this.mListThoughtIds = listIds;
        this.mListThoughts = listMessages;
        this.mListDateAndTime = listDateAndTime;
        this.mListThoughtSaved = listSaved;
        this.mListThoughtSeen = listSeen;
        this.mMapViewAnimation = new HashMap<>();

        mDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        mTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        mCurrentLocale = locale;
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
        Calendar calendar = Calendar.getInstance(mCurrentLocale);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date today = calendar.getTime();

        long dateAndTime = Long.parseLong(mListDateAndTime.get(position));
        Date thoughtDate = new Date(dateAndTime);

        if (thoughtDate.before(today))
            viewHolder.mTextViewTime.setText(mTimeFormat.format(thoughtDate));
        else
            viewHolder.mTextViewTime.setText(mDateFormat.format(thoughtDate));

        if (mMapViewAnimation.containsKey(viewHolder.itemView))
        {
            Animator animator = mMapViewAnimation.remove(viewHolder.itemView);
            animator.end();
        }
        if (mListThoughtSeen.get(position))
        {
            mListThoughtSeen.set(position, true);
            startNewThoughtAnimation(viewHolder.itemView);
        }

        viewHolder.mTextViewThought.setText(mListThoughts.get(position));
        viewHolder.mImageViewThought.setColorFilter((mListThoughtSaved.get(position))
                ? mCallback.getColor(R.color.thought_saved)
                : mCallback.getColor(R.color.thought_not_saved), PorterDuff.Mode.MULTIPLY);
        viewHolder.mImageViewThought.setTag(position);
        viewHolder.mImageViewThought.setOnClickListener(this);
    }

    @Override
    public int getItemCount()
    {
        return mListThoughtIds.size();
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
        notifyItemChanged(position);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        mCallback = null;
    }

    /**
     * Starts a short pulsing animation for new thoughts.
     *
     * @param rootView view to animate
     */
    private void startNewThoughtAnimation(final View rootView)
    {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                mCallback.getColor(R.color.primary_color_light),
                mCallback.getColor(R.color.thought_unread));
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                rootView.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mMapViewAnimation.remove(rootView);
            }
        });
        mMapViewAnimation.put(rootView, colorAnimation);
        colorAnimation.start();
    }

    /**
     * Defines a new locale for dates.
     *
     * @param locale new locale
     */
    public void setLocale(Locale locale)
    {
        mDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        mTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        mCurrentLocale = locale;
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
         */
        public ThoughtViewHolder(View itemView)
        {
            super(itemView);
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
    }
}
