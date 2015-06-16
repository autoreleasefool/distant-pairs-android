package ca.josephroque.partners.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import ca.josephroque.partners.R;
import ca.josephroque.partners.util.AccountUtil;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link RegisterFragment.LoginCallbacks} interface to handle interaction events. Use the {@link
 * RegisterFragment#newInstance} factory method to create an instance of this fragment.
 */
public class RegisterFragment
        extends Fragment
        implements View.OnClickListener
{

    /** Represents boolean indicating if fragment is for user or pair registration. */
    private static final String REGISTER_OR_PAIR = "reg_or_pair";

    /** User input to suggest they wish to register. */
    private Button mButtonRegister;
    /** User input to suggest they wish to check for pair requests. */
    private Button mButtonPairCheck;
    /** User input for username. */
    private EditText mEditTextUsername;
    /** Container for user input views. */
    private LinearLayout mLinearLayoutRoot;
    /** To display progress when contacting server. */
    private ProgressDialog mProgressDialogServer;

    /** Instance of callback interface. */
    private LoginCallbacks mCallback;

    /** Indicates if fragment is for user or pair registration. */
    private boolean mRegisterOrPair;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static RegisterFragment newInstance()
    {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        if (savedInstanceState != null)
        {
            mRegisterOrPair = savedInstanceState.getBoolean(REGISTER_OR_PAIR);
        }

        mButtonRegister = (Button) rootView.findViewById(R.id.btn_login_register);
        mButtonPairCheck = (Button) rootView.findViewById(R.id.btn_check_pairs);
        mEditTextUsername = (EditText) rootView.findViewById(R.id.et_username);
        mLinearLayoutRoot = (LinearLayout) rootView.findViewById(R.id.ll_login);

        if (mRegisterOrPair)
        {
            mButtonRegister.setText(R.string.text_register);
            mButtonPairCheck.setVisibility(View.GONE);
        }
        else
        {
            mButtonRegister.setText(R.string.text_set_pair);
            mButtonPairCheck.setVisibility(View.VISIBLE);
        }

        mButtonRegister.setOnClickListener(this);
        mButtonPairCheck.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mCallback = (LoginCallbacks) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement LoginCallbacks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(REGISTER_OR_PAIR, mRegisterOrPair);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onClick(View src)
    {
        if (src == mButtonRegister)
        {
            final String username = AccountUtil.validateUsername(
                    mEditTextUsername.getText().toString());

            if (username == null)
            {
                // invalid username
            }
            // TODO: register
        }
        else if (src == mButtonPairCheck)
        {
            // TODO: register pair
        }
    }

    /**
     * Hides views and shows the progress bar.
     *
     * @param title title of progress bar
     * @param message message for progress bar
     */
    private void showProgressBar(@NonNull String title, @Nullable String message)
    {
        mLinearLayoutRoot.setVisibility(View.GONE);
        mProgressDialogServer = ProgressDialog.show(getActivity(), title, message, true, false);
    }

    /**
     * Hides progress bar and shows views.
     */
    private void hideProgressBar()
    {
        mLinearLayoutRoot.setVisibility(View.VISIBLE);
        mProgressDialogServer.dismiss();
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface LoginCallbacks
    {
    }

}
