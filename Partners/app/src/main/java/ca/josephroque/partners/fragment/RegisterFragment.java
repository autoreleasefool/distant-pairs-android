package ca.josephroque.partners.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.DisplayUtil;
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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
        mEditTextUsername = (EditText) rootView.findViewById(R.id.et_username);
        mRelativeLayoutRegister = (RelativeLayout) rootView.findViewById(R.id.rl_login);

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
                ErrorUtil.displayErrorDialog(getActivity(), "Invalid username",
                        "Username must be 16 characters or less and can only contain letters or"
                                + " numbers. Please, try again.");
                return;
            }

            if (mRegisterOrPair)
                new RegisterAccountTask().execute(username,
                        AccountUtil.randomAlphaNumericPassword());
            else
                new RegisterPartnerTask().execute(username);
        }
        else if (src == mButtonPairCheck && !mRegisterOrPair)
        {
            new PairCheckTask().execute();
        }
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
            ErrorUtil.displayErrorDialog(getActivity(), "No requests",
                    "Nobody has requested to be your pair.");
            mRelativeLayoutRegister.setVisibility(View.VISIBLE);
            return;
        }

        final ParseObject request = requests.next();
        final String requestUsername = request.getString(AccountUtil.USERNAME);

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
                        .getString(AccountUtil.USERNAME, null);

        if (accountUsername == null)
            return;

        ParseQuery<ParseObject> pairQuery = new ParseQuery<>("Pair");
        pairQuery.whereEqualTo(AccountUtil.PAIR, accountUsername);
        if (exception != null)
            pairQuery.whereNotEqualTo(AccountUtil.USERNAME, exception);

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
                    ErrorUtil.displayErrorDialog(getActivity(), "Error",
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
            DisplayUtil.hideKeyboard(getActivity());
            mRelativeLayoutRegister.setVisibility(View.GONE);
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
                    .putString(AccountUtil.USERNAME, credentials[0])
                    .putString(AccountUtil.PASSWORD, credentials[1])
                    .putString(AccountUtil.PARSE_USER_ID, parseUser.getObjectId())
                    .commit();

            return AccountUtil.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            Log.i(TAG, "Result: " + result);
            ((ProgressActivity) getActivity()).hideProgressBar();

            switch (result)
            {
                case AccountUtil.SUCCESS:
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
                    ErrorUtil.displayErrorDialog(getActivity(), "Connection failed",
                            "Failed to connect to the server. Please, try again. If this error"
                                    + "persists, your connection may not be sufficient.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtil.displayErrorDialog(getActivity(), "Username taken",
                            "That username is already in use. Please, try another.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                default:
                    ErrorUtil.displayErrorDialog(getActivity(), "Error",
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
            DisplayUtil.hideKeyboard(getActivity());
            mRelativeLayoutRegister.setVisibility(View.GONE);
            ((ProgressActivity) getActivity()).showProgressBar(R.string.text_checking);
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String accountUsername = preferences.getString(AccountUtil.USERNAME, null);

            if (accountUsername == null)
                return ParseException.USERNAME_MISSING;

            ParseQuery<ParseObject> pairQuery = new ParseQuery<>("Pair");
            pairQuery.whereEqualTo(AccountUtil.PAIR, accountUsername);

            try
            {
                mListPairRequests = pairQuery.find();
                return AccountUtil.SUCCESS;
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
                case AccountUtil.SUCCESS:
                    displayPairRequests(mListPairRequests.iterator());
                    break;
                case ParseException.OBJECT_NOT_FOUND:
                    ErrorUtil.displayErrorDialog(getActivity(), "No requests",
                            "Nobody has requested to be your pair.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorDialog(getActivity(), "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                default:
                    ErrorUtil.displayErrorDialog(getActivity(), "Unknown error",
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
            DisplayUtil.hideKeyboard(getActivity());
            mRelativeLayoutRegister.setVisibility(View.GONE);
            ((ProgressActivity) getActivity()).showProgressBar(R.string.text_registering);
        }

        @Override
        protected Integer doInBackground(String... partnerName)
        {
            final SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String username = preferences.getString(AccountUtil.USERNAME, null);
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
                pairQuery.whereEqualTo(AccountUtil.USERNAME, partnerName[0]);
                List<ParseObject> pairResult = pairQuery.find();

                String accountUsername = preferences.getString(AccountUtil.USERNAME, null);

                // Pair is invalid or already registered to someone else
                if (pairResult.size() > 1 || (pairResult.size() == 1
                        && !pairResult.get(0).get(AccountUtil.PAIR).equals(accountUsername)))
                    return ParseException.USERNAME_TAKEN;

                ParseObject parseObject = new ParseObject("Pair");
                parseObject.put(AccountUtil.USERNAME, accountUsername);
                parseObject.put(AccountUtil.PAIR, partnerName[0]);
                parseObject.save();

                deletePendingPairRequests(partnerName[0]);

                preferences.edit()
                        .putString(AccountUtil.PAIR, partnerName[0])
                        .putString(AccountUtil.PARSE_PAIR_ID, result.get(0).getObjectId())
                        .apply();

                return AccountUtil.SUCCESS;
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
            Log.i(TAG, "Register partner result: " + result);

            switch (result)
            {
                case AccountUtil.SUCCESS:
                    mCallback.pairRegistered();
                    return;
                case ParseException.UNSUPPORTED_SERVICE:
                    ErrorUtil.displayErrorDialog(getActivity(), "Invalid name",
                            "You cannot add yourself as your pair.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorDialog(getActivity(), "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.USERNAME_MISSING:
                    ErrorUtil.displayErrorDialog(getActivity(), "Error registering pair",
                            "This user does not exist. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtil.displayErrorDialog(getActivity(), "Error registering pair",
                            "This user already has a pair. Please, try again.");
                    mRelativeLayoutRegister.setVisibility(View.VISIBLE);
                    break;
                default:
                    ErrorUtil.displayErrorDialog(getActivity(), "Error registering pair",
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
