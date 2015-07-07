package ca.josephroque.partners.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Iterator;
import java.util.List;

import ca.josephroque.partners.ProgressActivity;
import ca.josephroque.partners.R;
import ca.josephroque.partners.util.AccountUtils;
import ca.josephroque.partners.util.DisplayUtils;
import ca.josephroque.partners.util.ErrorUtils;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link RegisterFragment.RegisterCallbacks} interface to handle interaction events. Use the {@link
 * RegisterFragment#newInstance} factory method to create an instance of this fragment.
 */
public class RegisterFragment
        extends Fragment
        implements View.OnClickListener
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "RegisterFragment";

    /** Represents boolean indicating if fragment is for user or pair registration. */
    private static final String REGISTER_OR_PAIR = "reg_or_pair";

    /** User input to suggest they wish to register. */
    private Button mButtonRegister;
    /** User input to suggest they wish to check for pair requests. */
    private Button mButtonPairCheck;
    /** User input for username. */
    private EditText mEditTextUsername;
    /** Container for user input views. */
    private RelativeLayout mRelativeLayoutRegister;

    /** Instance of callback interface. */
    private RegisterCallbacks mCallback;

    /** Indicates if fragment is for user or pair registration. */
    private boolean mRegisterOrPair;

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param registerOrPair true if registering a user, false if registering a pair
     * @return A new instance of fragment LoginFragment.
     */
    public static RegisterFragment newInstance(boolean registerOrPair)
    {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putBoolean(REGISTER_OR_PAIR, registerOrPair);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("deprecation")    // Uses updated methods in APIs where available
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        if (savedInstanceState != null)
            mRegisterOrPair = savedInstanceState.getBoolean(REGISTER_OR_PAIR, true);
        else
            mRegisterOrPair = getArguments().getBoolean(REGISTER_OR_PAIR, true);

        mButtonRegister = (Button) rootView.findViewById(R.id.btn_login_register);
        mButtonPairCheck = (Button) rootView.findViewById(R.id.btn_check_pairs);
        mRelativeLayoutRegister = (RelativeLayout) rootView.findViewById(R.id.rl_login);

        if (mRegisterOrPair)
        {
            mButtonRegister.setText(R.string.text_register);
            mButtonPairCheck.setVisibility(View.GONE);
            rootView.findViewById(R.id.tv_login_prompt).setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonRegister.setText(R.string.text_set_pair);
            mButtonPairCheck.setVisibility(View.VISIBLE);
        }

        mButtonRegister.setOnClickListener(this);
        mButtonPairCheck.setOnClickListener(this);

        setupEditTextLayout(rootView);

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
            final String username = AccountUtils.validateUsername(
                    mEditTextUsername.getText().toString());

            if (username == null)
            {
                ErrorUtils.displayErrorDialog(getActivity(), "Invalid username",
                        "Username must be 16 characters or less and can only contain letters or"
                                + " numbers. Please, try again.");
                return;
            }

            if (mRegisterOrPair)
                new RegisterAccountTask().execute(username,
                        AccountUtils.randomAlphaNumericPassword());
            else
                new RegisterPartnerTask().execute(username);
        }
        else if (src == mButtonPairCheck && !mRegisterOrPair)
        {
            new PairCheckTask().execute();
        }
    }

    /**
     * Sets up {@link TextInputLayout} for {@code mEditTextUsername}.
     *
     * @param rootView to get views
     */
    @SuppressWarnings("deprecation")
    private void setupEditTextLayout(View rootView)
    {
        mEditTextUsername = (EditText) rootView.findViewById(R.id.et_username);
        final TextInputLayout textInputLayout =
                (TextInputLayout) rootView.findViewById(R.id.textinput_username);

        mEditTextUsername.addTextChangedListener(new TextWatcher()
        {
            /** Indicates if the text input layout is displaying an error. */
            private boolean mHasErrorMessage = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // does nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // does nothing
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String input = s.toString();
                if (input.length() > 0 && !input.matches(AccountUtils.REGEX_VALID_USERNAME))
                {
                    if (!mHasErrorMessage)
                        textInputLayout.setError("Numbers and letters only");
                    mHasErrorMessage = true;
                }
                else if (input.length() > AccountUtils.USERNAME_MAX_LENGTH)
                {
                    if (!mHasErrorMessage)
                        textInputLayout.setError("Max length is 16 characters");
                    mHasErrorMessage = true;
                }
                else
                {
                    textInputLayout.setErrorEnabled(false);
                    mHasErrorMessage = false;
                }
            }
        });

        if (mRegisterOrPair)
            mEditTextUsername.setHint(R.string.text_hint_username);
        else
            mEditTextUsername.setHint(R.string.text_hint_pair);
    }

    /**
     * Displays prompt for user to accept a pair request.
     *
     * @param requests iterator for pair requests from parse server
     */
    private void displayPairRequests(final Iterator<ParseObject> requests)
    {
        if (!requests.hasNext())
        {
            ErrorUtils.displayErrorDialog(getActivity(), "No requests",
                    "Nobody has requested to be your pair.");
            mRelativeLayoutRegister.setVisibility(View.VISIBLE);
            return;
        }

        final ParseObject request = requests.next();
        final String requestUsername = request.getString(AccountUtils.USERNAME);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        requests.remove();
                        deletePendingPairRequests(requestUsername);
                        new RegisterPartnerTask().execute(requestUsername);
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        deletePendingPairRequests(null);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        requests.remove();
                        request.deleteInBackground();
                        displayPairRequests(requests);
                    default:
                        // does nothing
                }
            }
        };

        new AlertDialog.Builder(getActivity())
                .setTitle("Pair request")
                .setMessage("User '" + requestUsername + "' has requested to be your pair. "
                        + "Do you accept?")
                .setPositiveButton(R.string.text_dialog_okay, listener)
                .setNeutralButton(R.string.text_dialog_no_to_all, listener)
                .setNegativeButton(R.string.text_dialog_no, listener)
                .create()
                .show();
    }

    /**
     * Deletes requests on parse server to be user's pair.
     *
     * @param exception do not delete requests with this username
     */
    private void deletePendingPairRequests(String exception)
    {
        String accountUsername =
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(AccountUtils.USERNAME, null);

        if (accountUsername == null)
            return;

        ParseQuery<ParseObject> pairQuery = new ParseQuery<>("Pair");
        pairQuery.whereEqualTo(AccountUtils.PAIR, accountUsername);
        if (exception != null)
            pairQuery.whereNotEqualTo(AccountUtils.USERNAME, exception);

        pairQuery.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if (e == null)
                {
                    ParseObject.deleteAllInBackground(list);
                }
                else
                {
                    ErrorUtils.displayErrorDialog(getActivity(), "Error",
                            "Could not delete requests. Please, try again.");
                }
            }
        });
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
            DisplayUtils.hideKeyboard(getActivity());
            mRelativeLayoutRegister.setVisibility(View.GONE);
            if (getView() != null)
                getView().findViewById(R.id.tv_login_prompt).setVisibility(View.GONE);
            ((ProgressActivity) getActivity()).showProgressBar(R.string.text_registering);
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
                    .putString(AccountUtils.USERNAME, credentials[0])
                    .putString(AccountUtils.PASSWORD, credentials[1])
                    .commit();

            return AccountUtils.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            ((ProgressActivity) getActivity()).hideProgressBar();

            switch (result)
            {
                case AccountUtils.SUCCESS:
                    mCallback.login(new LoginCallback()
                    {
                        @Override
                        public void onLoginFailed(int errorCode)
                        {
                            mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtils.displayErrorDialog(getActivity(), "Connection failed",
                            "Failed to connect to the server. Please, try again. If this error"
                                    + "persists, your connection may not be sufficient.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtils.displayErrorDialog(getActivity(), "Username taken",
                            "That username is already in use. Please, try another.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                default:
                    ErrorUtils.displayErrorDialog(getActivity(), "Error",
                            "An error has occurred. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    /**
     * Checks Parse server for requests to be paired with this user.
     */
    private final class PairCheckTask
            extends AsyncTask<Void, Void, Integer>
    {

        /** List of results from parse server. */
        private List<ParseObject> mListPairRequests;

        @Override
        protected void onPreExecute()
        {
            DisplayUtils.hideKeyboard(getActivity());
            mRelativeLayoutRegister.setVisibility(View.GONE);
            ((ProgressActivity) getActivity()).showProgressBar(R.string.text_checking);
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String accountUsername = preferences.getString(AccountUtils.USERNAME, null);

            if (accountUsername == null)
                return ParseException.USERNAME_MISSING;

            ParseQuery<ParseObject> pairQuery = new ParseQuery<>("Pair");
            pairQuery.whereEqualTo(AccountUtils.PAIR, accountUsername);

            try
            {
                mListPairRequests = pairQuery.find();
                return AccountUtils.SUCCESS;
            }
            catch (ParseException ex)
            {
                return ex.getCode();
            }
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            ((ProgressActivity) getActivity()).hideProgressBar();
            if (mListPairRequests == null)
                result = ParseException.OBJECT_NOT_FOUND;

            switch (result)
            {
                case AccountUtils.SUCCESS:
                    displayPairRequests(mListPairRequests.iterator());
                    break;
                case ParseException.OBJECT_NOT_FOUND:
                    ErrorUtils.displayErrorDialog(getActivity(), "No requests",
                            "Nobody has requested to be your pair.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtils.displayErrorDialog(getActivity(), "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                default:
                    ErrorUtils.displayErrorDialog(getActivity(), "Unknown error",
                            "An unknown error occurred. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    /**
     * Registers account with a pair in the background.
     */
    private final class RegisterPartnerTask
            extends AsyncTask<String, Void, Integer>
    {

        @Override
        protected void onPreExecute()
        {
            DisplayUtils.hideKeyboard(getActivity());
            mRelativeLayoutRegister.setVisibility(View.GONE);
            ((ProgressActivity) getActivity()).showProgressBar(R.string.text_registering);
        }

        @Override
        protected Integer doInBackground(String... partnerName)
        {
            final SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String username = preferences.getString(AccountUtils.USERNAME, null);
            if (partnerName[0].equals(username))
                return ParseException.UNSUPPORTED_SERVICE;

            ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
            parseQuery.whereEqualTo("username", partnerName[0]);

            try
            {
                List<ParseUser> result = parseQuery.find();
                if (result.size() == 0)
                    return ParseException.USERNAME_MISSING;

                ParseQuery<ParseObject> pairQuery = new ParseQuery<>("Pair");
                pairQuery.whereEqualTo(AccountUtils.USERNAME, partnerName[0]);
                List<ParseObject> pairResult = pairQuery.find();

                String accountUsername = preferences.getString(AccountUtils.USERNAME, null);

                // Pair is invalid or already registered to someone else
                if (pairResult.size() > 1 || (pairResult.size() == 1
                        && !pairResult.get(0).get(AccountUtils.PAIR).equals(accountUsername)))
                    return ParseException.USERNAME_TAKEN;

                ParseObject parseObject = new ParseObject("Pair");
                parseObject.put(AccountUtils.USERNAME, accountUsername);
                parseObject.put(AccountUtils.PAIR, partnerName[0]);
                parseObject.save();

                deletePendingPairRequests(partnerName[0]);

                preferences.edit()
                        .putString(AccountUtils.PAIR, partnerName[0])
                        .apply();

                return AccountUtils.SUCCESS;
            }
            catch (ParseException ex)
            {
                return ex.getCode();
            }
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            ((ProgressActivity) getActivity()).hideProgressBar();

            switch (result)
            {
                case AccountUtils.SUCCESS:
                    mCallback.pairRegistered();
                    return;
                case ParseException.UNSUPPORTED_SERVICE:
                    ErrorUtils.displayErrorDialog(getActivity(), "Invalid name",
                            "You cannot add yourself as your pair.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtils.displayErrorDialog(getActivity(), "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.USERNAME_MISSING:
                    ErrorUtils.displayErrorDialog(getActivity(), "Error registering pair",
                            "This user does not exist. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtils.displayErrorDialog(getActivity(), "Error registering pair",
                            "This user already has a pair. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                default:
                    ErrorUtils.displayErrorDialog(getActivity(), "Error registering pair",
                            "This user may not exist or may already be paired. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
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

        /**
         * Invoked when a pair is successfully registered.
         */
        void pairRegistered();
    }

    /**
     * Methods which can be overridden to provide a result upon completion or failure of a login.
     */
    public interface LoginCallback
    {

        /**
         * Invoked when the login fails.
         *
         * @param errorCode error
         */
        void onLoginFailed(int errorCode);
    }
}
