package ca.josephroque.partners.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ca.josephroque.partners.R;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link LoginFragment.LoginCallbacks} interface to handle interaction events. Use the {@link
 * LoginFragment#newInstance} factory method to create an instance of this fragment.
 */
public class LoginFragment
        extends Fragment
        implements View.OnClickListener
{

    /** Username input from user. */
    private EditText mEditTextUsername;

    /** Instance of callback interface. */
    private LoginCallbacks mCallback;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance()
    {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mEditTextUsername = (EditText) rootView.findViewById(R.id.et_username);
        rootView.findViewById(R.id.btn_register).setOnClickListener(this);

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
    public void onDetach()
    {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_register:
                String accountName = mEditTextUsername.getText().toString();
                mCallback.registerAccount(accountName);
                break;
            default:
                throw new IllegalArgumentException("not a valid view for on click");
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface LoginCallbacks
    {

        /**
         * Registers a new account with the server and opens the new fragment.
         * @param accountName name for new account
         */
        void registerAccount(String accountName);
    }

}
