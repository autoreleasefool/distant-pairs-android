package ca.josephroque.partners;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;

import ca.josephroque.partners.fragment.RegisterFragment;
import ca.josephroque.partners.message.MessageService;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;

/**
 * Provides interface for user registration and login.
 */
public class LoginActivity
        extends AppCompatActivity
        implements RegisterFragment.RegisterCallbacks
{

    /** Displays progress when connecting to server. */
    private ProgressBar mProgressBarServer;
    /** Displays action when connecting to server. */
    private TextView mTextViewServer;

    /** Intent to initiate instance of {@link MessageService}. */
    private Intent mIntentMessageService;
    /** Intent to initiate instance of {@link PartnerActivity}. */
    private Intent mIntentPartnerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mIntentMessageService = new Intent(LoginActivity.this, MessageService.class);
        mIntentPartnerActivity = new Intent(LoginActivity.this, PartnerActivity.class);

        if (ParseUser.getCurrentUser() != null)
        {
            startService(mIntentMessageService);
            startActivity(mIntentPartnerActivity);
            finish();
            return;
        }

        if (savedInstanceState == null)
        {
            // Fragment has not been created yet
            RegisterFragment fragment = RegisterFragment.newInstance(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, fragment)
                    .commit();
        }
    }

    @Override
    public void login(final RegisterFragment.LoginCallback callback)
    {
        new LoginTask().execute(callback);
    }

    @Override
    public void pairRegistered()
    {
        throw new UnsupportedOperationException("cannot register pair - not logged in");
    }

    /**
     * Creates and shows a progress bar.
     *
     * @param message id of string for progress bar
     */
    private void showProgressBar(int message)
    {
        if (mProgressBarServer == null)
            mProgressBarServer = (ProgressBar) findViewById(R.id.pb_login);
        if (mTextViewServer == null)
            mTextViewServer = (TextView) findViewById(R.id.tv_login);

        mProgressBarServer.setVisibility(View.VISIBLE);
        mTextViewServer.setVisibility(View.VISIBLE);
        mTextViewServer.setText(message);

    }

    /**
     * Hides progress bar.
     */
    private void hideProgressBar()
    {
        mProgressBarServer.setVisibility(View.GONE);
        mTextViewServer.setVisibility(View.GONE);
    }

    /**
     * Logs a user into Parse in the background.
     */
    private final class LoginTask
            extends AsyncTask<RegisterFragment.LoginCallback, Void, Integer>
    {

        /** Instance of callback interface for result. */
        private RegisterFragment.LoginCallback mCallback;

        @Override
        protected void onPreExecute()
        {
            showProgressBar(R.string.text_logging_in);
        }

        @Override
        protected Integer doInBackground(
                RegisterFragment.LoginCallback... callback)
        {
            if (callback != null)
                mCallback = callback[0];

            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            final String accountName = preferences.getString(AccountUtil.USERNAME, null);
            final String accountPass = preferences.getString(AccountUtil.PASSWORD, null);

            if (accountName == null || accountPass == null)
            {
                return ParseException.USERNAME_MISSING;
            }

            try
            {
                ParseUser.logIn(accountName, accountPass);
            }
            catch (ParseException ex)
            {
                return ex.getCode();
            }

            return AccountUtil.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            hideProgressBar();

            if (mCallback != null && result != AccountUtil.SUCCESS)
                mCallback.onLoginFailed(result);

            switch (result)
            {
                case AccountUtil.SUCCESS:
                    startService(mIntentMessageService);
                    startActivity(mIntentPartnerActivity);
                    finish();
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Connection failed",
                            "Failed to connect to the server. Please, try again. If this error"
                                    + "persists, your connection may not be sufficient.");
                    break;
                case ParseException.OBJECT_NOT_FOUND:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Incorrect credentials",
                            "Your account is no longer valid. Please, create another.");
                    AccountUtil.deleteAccount(LoginActivity.this);
                    break;
                case ParseException.USERNAME_MISSING:
                    // does nothing
                    break;
                default:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Error",
                            "An error has occurred. Please, try again.");
            }
        }
    }
}
