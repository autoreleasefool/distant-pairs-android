package ca.josephroque.partners.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.josephroque.partners.R;
import ca.josephroque.partners.interfaces.ActionButtonHandler;
import ca.josephroque.partners.interfaces.MessageHandler;
import ca.josephroque.partners.util.AccountUtil;

/**
 * A simple {@link Fragment} subclass. Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeartFragment
        extends Fragment
        implements ActionButtonHandler, MessageHandler
{

    /** Container for progress bar. */
    private LinearLayout mLinearLayoutConnection;
    /** Displays a message with the progress bar. */
    private TextView mTextViewConnection;

    /** Parse object id of pair. */
    private String mPairId;

    /** Indicates if the progress bar is visible. */
    private boolean mProgressBarActive = false;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param pairId Parse object id of user's pair
     * @return A new instance of fragment HeartFragment
     */
    public static HeartFragment newInstance(String pairId)
    {
        HeartFragment heartFragment = new HeartFragment();
        Bundle args = new Bundle();
        args.putString(AccountUtil.PARSE_PAIR_ID, pairId);
        heartFragment.setArguments(args);
        return heartFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_heart, container, false);

        mLinearLayoutConnection = (LinearLayout) rootView.findViewById(R.id.ll_progress);
        mTextViewConnection = (TextView) rootView.findViewById(R.id.tv_connection);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void handleActionClick()
    {
        if (mProgressBarActive)
            return;
        // TODO: send thought
    }

    @Override
    public void onNewMessage(String dateAndTime, String message)
    {
        // TODO: display new thought
    }

    @Override
    public void onMessageFailed(String message)
    {
        // TODO: message failed
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
