package ca.josephroque.partners.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import ca.josephroque.partners.R;
import ca.josephroque.partners.util.DisplayUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialFragment
        extends Fragment
{

    /** Number of pages in the tutorial. */
    public static final int TUTORIAL_PAGES = 4;

    /** Tutorial page for registering. */
    private static final int TUTORIAL_REGISTER = 0;
    /** Tutorial page for registering a pair. */
    private static final int TUTORIAL_REGISTER_PAIR = 1;
    /** Tutorial page for logging in. */
    private static final int TUTORIAL_LOG_IN = 2;
    /** Tutorial page for leaving a thought. */
    private static final int TUTORIAL_THOUGHT = 3;

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

        setupTutorialPageAndText();
        setTutorialLayout(rootView);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

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
     */
    private void setupTutorialPageAndText()
    {
        final int dp16 = DisplayUtil.convertDpToPx(getActivity(), 16);

        mTextViewTutorial = new TextView(getActivity().getApplicationContext());
        mTextViewTutorial.setId(R.id.tv_tutorial);
        mTextViewTutorial.setPadding(dp16, dp16, dp16, dp16);
        mTextViewTutorial.setGravity(Gravity.CENTER_HORIZONTAL);
        mTextViewTutorial.setTextAppearance(getActivity().getApplicationContext(),
                android.R.style.TextAppearance_Large);

        EditText editText;
        Drawable[] drawables;

        switch (mTutorialPage)
        {
            case TUTORIAL_REGISTER:
                mTextViewTutorial.setText(R.string.text_tutorial_register);
                mViewTutorial = View.inflate(getActivity().getApplicationContext(),
                        R.layout.tutorial_register, null);
                editText = (EditText) mViewTutorial.findViewById(R.id.et_username);
                drawables = editText.getCompoundDrawables();
                drawables[0].setColorFilter(getResources().getColor(R.color.person_filter),
                        PorterDuff.Mode.MULTIPLY);
                break;
            case TUTORIAL_REGISTER_PAIR:
                mTextViewTutorial.setText(R.string.text_tutorial_register_pair);
                mViewTutorial = View.inflate(getActivity().getApplicationContext(),
                        R.layout.tutorial_register_pair, null);
                editText = (EditText) mViewTutorial.findViewById(R.id.et_username);
                drawables = editText.getCompoundDrawables();
                drawables[0].setColorFilter(getResources().getColor(R.color.pair_filter),
                        PorterDuff.Mode.MULTIPLY);
                break;
            case TUTORIAL_LOG_IN:
                mTextViewTutorial.setText(R.string.text_tutorial_login);
                mViewTutorial = View.inflate(getActivity().getApplicationContext(),
                        R.layout.tutorial_login, null);
                break;
            case TUTORIAL_THOUGHT:
                mTextViewTutorial.setText(R.string.text_tutorial_thought);
                mViewTutorial = View.inflate(getActivity().getApplicationContext(),
                        R.layout.tutorial_thought, null);
                break;
            default:
                throw new IllegalStateException("invalid tutorial page: " + mTutorialPage);
        }

        mViewTutorial.setVisibility(View.INVISIBLE);
        mTextViewTutorial.setVisibility(View.INVISIBLE);
    }

    /**
     * Sets the layout to alternate between two setups depending on the page.
     *
     * @param rootView root to attach views to
     */
    private void setTutorialLayout(RelativeLayout rootView)
    {
        RelativeLayout.LayoutParams layoutParams;
        rootView.removeAllViews();

        if (mTutorialPage % 2 == 0)
        {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rootView.addView(mTextViewTutorial, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.tv_tutorial);
            rootView.addView(mViewTutorial, layoutParams);
        }
        else
        {
            Space emptySpace = new Space(getActivity().getApplicationContext());
            emptySpace.setId(R.id.space_tutorial);
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getActivity().findViewById(R.id.rl_splash_toolbar).getHeight());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rootView.addView(emptySpace, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.space_tutorial);
            rootView.addView(mTextViewTutorial, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.tv_tutorial);
            rootView.addView(mViewTutorial, layoutParams);
        }
    }

    /**
     * Begins an animation to show the two views which describe the step in the tutorial.
     */
    public void startAnimation()
    {
        if (mAnimationCompleted)
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
                        boolean topToBottom = mTutorialPage % 2 == 0;
                        mViewTutorial.setAlpha(0f);
                        mViewTutorial.setVisibility(View.VISIBLE);
                        mViewTutorial.setY(mViewTutorial.getY()
                                + ANIMATION_POSITION_OFFSET * ((topToBottom)
                                ? 1
                                : -1));
                        mViewTutorial.animate()
                                .alpha(1f)
                                .yBy(ANIMATION_POSITION_OFFSET * ((topToBottom)
                                        ? -1
                                        : 1))
                                .setDuration(duration)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    }
                })
                .start();
    }
}
