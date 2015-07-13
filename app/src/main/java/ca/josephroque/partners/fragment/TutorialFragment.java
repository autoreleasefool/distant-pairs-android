package ca.josephroque.partners.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.josephroque.partners.R;
import ca.josephroque.partners.util.DisplayUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialFragment
        extends Fragment
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "TutorialFragment";

    /** Number of pages in the tutorial. */
    public static final int TUTORIAL_PAGES = 5;

    /** Tutorial page for registering. */
    private static final int TUTORIAL_REGISTER = 0;
    /** Tutorial page for registering pair. */
    private static final int TUTORIAL_REGISTER_PAIR = 1;
    /** Tutorial page for logging in. */
    private static final int TUTORIAL_LOG_IN = 2;
    /** Tutorial page for leaving a thought. */
    private static final int TUTORIAL_THOUGHT = 3;
    /** Tutorial page for saving a thought. */
    private static final int TUTORIAL_THOUGHT_SAVED = 4;

    /** Distance to animate position offset of views. */
    private static final int ANIMATION_POSITION_OFFSET = 50;

    /** Represents page of the tutorial displayed by this fragment. */
    private static final String ARG_PAGE = "arg_page";

    /** Displays layout to showcase tutorial step. */
    private View mViewTutorial;
    /** Explains the tutorial step. */
    private TextView mTextViewTutorial;

    /** Page of the tutorial shown in this fragment. */
    private int mTutorialPage;
    /** Indicates if the animation has been run. */
    private boolean mAnimationCompleted;
    /** Indicates if the current device is a tablet. */
    private boolean mIsTablet = false;
    /** Indicates if the fragment was restored from a previous instance. */
    private boolean mFromSavedInstanceState = false;

    /**
     * Creates a new instance of TutorialFragment.
     *
     * @param page page of tutorial
     * @return new instance
     */
    public static TutorialFragment newInstance(int page)
    {
        if (page < 0 || page >= TUTORIAL_PAGES)
            throw new IllegalArgumentException("page must be between 0 and "
                    + (TUTORIAL_PAGES - 1));

        TutorialFragment instance = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
            Log.i(TAG, "From instance state");
        } else {
            Log.i(TAG, "Not from instance state");
        }
        if (!getResources().getBoolean(R.bool.portrait_only)) {
            mIsTablet = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        RelativeLayout rootView =
                (RelativeLayout) inflater.inflate(R.layout.fragment_tutorial, container, false);

        Bundle args = (savedInstanceState == null)
                ? getArguments()
                : savedInstanceState;
        mTutorialPage = args.getInt(ARG_PAGE);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        setupTutorialPageAndText((RelativeLayout) getView());
        if (isVisible() && mTutorialPage == 0)
            startAnimation();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_PAGE, mTutorialPage);
    }

    /**
     * Initializes the text and image objects for the tutorial.
     *
     * @param rootView root to attach views to
     */
    private void setupTutorialPageAndText(RelativeLayout rootView)
    {
        final int dp16 = DisplayUtils.convertDpToPx(getActivity(), 16);

        mTextViewTutorial = new TextView(getActivity());
        mTextViewTutorial.setId(R.id.tv_tutorial);
        mTextViewTutorial.setPadding(dp16, dp16, dp16, dp16);
        mTextViewTutorial.setGravity(Gravity.CENTER_HORIZONTAL);
        mTextViewTutorial.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);

        switch (mTutorialPage)
        {
            case TUTORIAL_REGISTER:
                mTextViewTutorial.setText(R.string.text_tutorial_register);
                setTutorialLayout(rootView);
                break;
            case TUTORIAL_REGISTER_PAIR:
                mTextViewTutorial.setText(R.string.text_tutorial_register_pair);
                setTutorialLayout(rootView);
                break;
            case TUTORIAL_LOG_IN:
                mTextViewTutorial.setText(R.string.text_tutorial_login);
                mViewTutorial = View.inflate(getActivity(), R.layout.tutorial_login, null);
                setTutorialLayout(rootView);
                break;
            case TUTORIAL_THOUGHT:
                mTextViewTutorial.setText(R.string.text_tutorial_thought);
                mViewTutorial = View.inflate(getActivity(), R.layout.tutorial_thought, null);
                setTutorialLayout(rootView);
                break;
            case TUTORIAL_THOUGHT_SAVED:
                mTextViewTutorial.setText(R.string.text_tutorial_thought_saved);
                mViewTutorial = View.inflate(getActivity(), R.layout.tutorial_save, null);
                setTutorialLayout(rootView);
                break;
            default:
                throw new IllegalStateException("invalid tutorial page: " + mTutorialPage);
        }
    }

    /**
     * Sets the layout to alternate between two setups depending on the page.
     *
     * @param rootView root to attach views to
     */
    private void setTutorialLayout(RelativeLayout rootView)
    {
        final float maxTutorialWidth = getResources().getDimension(R.dimen.max_tutorial_width);
        RelativeLayout.LayoutParams layoutParams;
        rootView.removeAllViews();

        final int dp16 = DisplayUtils.convertDpToPx(getActivity(), 16);

        if (mViewTutorial != null)
        {
            if (!mFromSavedInstanceState) {
                mViewTutorial.setVisibility(View.INVISIBLE);
                mTextViewTutorial.setVisibility(View.INVISIBLE);
            }

            if (mIsTablet)
                layoutParams = new RelativeLayout.LayoutParams((int) maxTutorialWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            else
                layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(dp16, dp16, dp16, dp16);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rootView.addView(mViewTutorial, layoutParams);

            if (mIsTablet)
                layoutParams = new RelativeLayout.LayoutParams((int) maxTutorialWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            else
                layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.tutorial_content);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rootView.addView(mTextViewTutorial, layoutParams);
        }
        else
        {
            if (!mFromSavedInstanceState)
                mTextViewTutorial.setVisibility(View.INVISIBLE);

            if (mIsTablet)
                layoutParams = new RelativeLayout.LayoutParams((int) maxTutorialWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            else
                layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rootView.addView(mTextViewTutorial, layoutParams);
        }
    }

    /**
     * Begins an animation to show the two views which describe the step in the tutorial.
     */
    public void startAnimation()
    {
        if (mAnimationCompleted || mFromSavedInstanceState)
            return;

        mAnimationCompleted = true;
        final int duration = getResources().getInteger(android.R.integer.config_longAnimTime);

        mTextViewTutorial.setAlpha(0f);
        mTextViewTutorial.setVisibility(View.VISIBLE);
        mTextViewTutorial.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        if (mViewTutorial != null)
                        {
                            mViewTutorial.setAlpha(0f);
                            mViewTutorial.setVisibility(View.VISIBLE);
                            mViewTutorial.setY(mViewTutorial.getY()
                                    + ANIMATION_POSITION_OFFSET);
                            mViewTutorial.animate()
                                    .alpha(1f)
                                    .yBy(-ANIMATION_POSITION_OFFSET)
                                    .setDuration(duration)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();
                        }
                    }
                })
                .start();
    }
}
