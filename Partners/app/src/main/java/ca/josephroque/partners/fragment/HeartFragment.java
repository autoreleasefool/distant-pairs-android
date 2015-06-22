package ca.josephroque.partners.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import ca.josephroque.partners.PartnerActivity;
import ca.josephroque.partners.R;
import ca.josephroque.partners.interfaces.MessageHandler;
import ca.josephroque.partners.util.MessageUtil;

/**
 * A simple {@link Fragment} subclass. Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeartFragment
        extends Fragment
        implements MessageHandler
{

    /** Container for progress bar. */
    private LinearLayout mLinearLayoutConnection;
    /** Displays a message with the progress bar. */
    private TextView mTextViewConnection;
    /** Displays alternating colors of a heart. */
    private ViewSwitcher mViewSwitcherHeart;
    /** Displays most recent thought received. */
    private TextView mTextViewRecentThought;

    /** Indicates if the progress bar is visible. */
    private boolean mProgressBarActive = false;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment HeartFragment
     */
    public static HeartFragment newInstance()
    {
        return new HeartFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_heart, container, false);

        mLinearLayoutConnection = (LinearLayout) rootView.findViewById(R.id.ll_progress);
        mTextViewConnection = (TextView) rootView.findViewById(R.id.tv_connection);
        mTextViewRecentThought = (TextView) rootView.findViewById(R.id.tv_thought_most_recent);
        mViewSwitcherHeart = (ViewSwitcher) rootView.findViewById(R.id.switcher_heart);

        rootView.findViewById(R.id.cv_thought).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((PartnerActivity) getActivity()).showFragment(PartnerActivity.THOUGHT_FRAGMENT);
            }
        });

        return rootView;
    }

    @Override
    public void onNewMessage(final String dateAndTime, final String message)
    {
        if (MessageUtil.LOGIN_MESSAGE.equals(message))
        {
            if (mViewSwitcherHeart.getDisplayedChild() == 0)
                mViewSwitcherHeart.showNext();
        }
        else if (MessageUtil.LOGOUT_MESSAGE.equals(message))
        {
            if (mViewSwitcherHeart.getDisplayedChild() == 1)
                mViewSwitcherHeart.showPrevious();
        }
        else
        {
            mTextViewRecentThought.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mTextViewRecentThought.setText(message);
                }
            });
        }
    }

    /**
     * Displays the progress bar with a message.
     *
     * @param message id of string for message
     */
    private void showProgressBar(int message)
    {
        mProgressBarActive = true;
        mLinearLayoutConnection.setVisibility(View.VISIBLE);
        mTextViewConnection.setText(message);
    }

    /**
     * Hides the progress bar.
     */
    private void hideProgressBar()
    {
        mProgressBarActive = false;
        mLinearLayoutConnection.setVisibility(View.GONE);
    }
}
