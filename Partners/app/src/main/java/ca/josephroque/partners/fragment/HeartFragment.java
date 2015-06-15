package ca.josephroque.partners.fragment;


import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ca.josephroque.partners.FullscreenActivity;
import ca.josephroque.partners.R;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.UserStatusUtil;

/**
 * A simple {@link Fragment} subclass. Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeartFragment
        extends Fragment
        implements FullscreenActivity.MessageDisplay
{

    /** Displays heart indicating online status of partner. */
    private ImageView mImageViewHeart;
    /** Displays last message from partner. */
    private TextView mTextViewLastThought;

    /** Current user's name. */
    private String mAccountName;
    /** Current user's partner's name. */
    private String mParnerName;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment HeartFragment.
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

        mImageViewHeart = (ImageView) rootView.findViewById(R.id.iv_heart);
        mTextViewLastThought = (TextView) rootView.findViewById(R.id.tv_last_thought);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAccountName = prefs.getString(AccountUtil.USERNAME, null);
        mParnerName = prefs.getString(AccountUtil.PAIR, null);

        if (mAccountName == null || mParnerName == null)
        {
            ((FullscreenActivity) getActivity()).deleteAccount();
        }
    }

    @Override
    public void displayMessage(final String message)
    {
        if (UserStatusUtil.LOGIN_MESSAGE.equals(message))
        {
            mImageViewHeart.setColorFilter(getResources().getColor(R.color.heart_active),
                    PorterDuff.Mode.MULTIPLY);
            return;
        }
        else if (UserStatusUtil.LOGOUT_MESSAGE.equals(message))
        {
            mImageViewHeart.setColorFilter(getResources().getColor(R.color.heart_inactive),
                    PorterDuff.Mode.MULTIPLY);
            return;
        }

        mTextViewLastThought.post(new Runnable()
        {
            @Override
            public void run()
            {
                mTextViewLastThought.setText(message);
            }
        });
    }
}
