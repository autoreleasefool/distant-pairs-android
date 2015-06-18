package ca.josephroque.partners.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.parse.ParseException;
import com.parse.ParseUser;

import ca.josephroque.partners.R;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link RegisterFragment.RegisterCallbacks} interface to handle interaction events. Use the {@link
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
    private RegisterCallbacks mCallback;

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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("deprecation")    // Uses updated methods in APIs where available
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

            final Drawable editTextDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                editTextDrawable = getResources().getDrawable(R.drawable.ic_person, null);
            else
                editTextDrawable = getResources().getDrawable(R.drawable.ic_person);

            if (editTextDrawable != null)
                editTextDrawable.setColorFilter(getResources().getColor(R.color.person_filter),
                        PorterDuff.Mode.MULTIPLY);

            mEditTextUsername.setCompoundDrawables(editTextDrawable, null, null, null);
        }
        else
        {
            mButtonRegister.setText(R.string.text_set_pair);
            mButtonPairCheck.setVisibility(View.VISIBLE);

            final Drawable editTextDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                editTextDrawable = getResources().getDrawable(R.drawable.ic_pair, null);
            else
                editTextDrawable = getResources().getDrawable(R.drawable.ic_pair);

            if (editTextDrawable != null)
                editTextDrawable.setColorFilter(getResources().getColor(R.color.pair_filter),
                        PorterDuff.Mode.MULTIPLY);

            mEditTextUsername.setCompoundDrawables(editTextDrawable, null, null, null);
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
            mCallback = (RegisterCallbacks) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement RegisterCallbacks");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_register, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete_account).setVisible(!mRegisterOrPair);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_delete_account:
                AccountUtil.promptDeleteAccount(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                ErrorUtil.displayErrorMessage(getActivity(), "Invalid username",
                        "Username must be 16 characters or less and can only contain letters or"
                                + " numbers. Please, try again.");
                return;
            }

            new RegisterAccountTask().execute(username, AccountUtil.randomAlphaNumericPassword());
        }
        else if (src == mButtonPairCheck && !mRegisterOrPair)
        {

        }
    }

    /**
     * Creates and shows a progress bar.
     *
     * @param title title of progress bar
     * @param message message for progress bar
     */
    private void showProgressBar(@NonNull String title, @Nullable String message)
    {
        mProgressDialogServer = ProgressDialog.show(getActivity(), title, message, true, false);
    }

    /**
     * Hides progress bar.
     */
    private void hideProgressBar()
    {
        mProgressDialogServer.dismiss();
        mProgressDialogServer = null;
    }

    /**
     * Registers a new Parse account in a background thread.
     */
    private final class RegisterAccountTask
            extends AsyncTask<String, Void, Integer>
    {

        @Override
        protected void onPreExecute()
        {
            mLinearLayoutRoot.setVisibility(View.GONE);
            showProgressBar(getResources().getString(R.string.text_registering), null);
        }

        @Override
        protected Integer doInBackground(String... credentials)
        {
            ParseUser parseUser = new ParseUser();
            parseUser.setUsername(credentials[0]);
            parseUser.setPassword(credentials[1]);

            try
            {
                parseUser.signUp();
            }
            catch (ParseException ex)
            {
                return ex.getCode();
            }

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putString(AccountUtil.USERNAME, credentials[0])
                    .putString(AccountUtil.PASSWORD, credentials[1])
                    .commit();

            return AccountUtil.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            hideProgressBar();

            switch (result)
            {
                case AccountUtil.SUCCESS:
                    mCallback.login(new LoginCallback() {
                        @Override
                        public void onLoginFailed(int errorCode)
                        {
                            mLinearLayoutRoot.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorMessage(getActivity(), "Connection failed",
                            "Failed to connect to the server. Please, try again. If this error"
                                    + "persists, your connection may not be sufficient.");
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtil.displayErrorMessage(getActivity(), "Username taken",
                            "That username is already in use. Please, try another.");
                    break;
                default:
                    ErrorUtil.displayErrorMessage(getActivity(), "Error",
                            "An error has occurred. Please, try again.");
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface RegisterCallbacks
    {

        /**
         * Logs into Parse user account.
         *
         * @param callback callback for login results
         */
        void login(LoginCallback callback);
    }

    /**
     * Methods which can be overridden to provide a result upon completion or failure of a login.
     */
    public interface LoginCallback
    {

        /**
         * Invoked when the login fails.
         */
        void onLoginFailed(int errorCode);
    }
}
