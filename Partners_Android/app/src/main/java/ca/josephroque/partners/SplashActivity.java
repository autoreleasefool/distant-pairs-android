package ca.josephroque.partners;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import ca.josephroque.partners.fragment.RegisterFragment;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;
import ca.josephroque.partners.util.MessageUtil;

/**
 * Provides interface for user registration and login.
 */
public class SplashActivity
        extends ProgressActivity
        implements RegisterFragment.RegisterCallbacks
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "LoginActivity";

    /** Displays progress when connecting to server. */
    private LinearLayout mLinearLayoutProgress;
    /** Displays action when connecting to server. */
    private TextView mTextViewProgress;
    /** Fragment container. */
    private FrameLayout mFrameLayoutContainer;

    /** Intent to initiate instance of {@link PartnerActivity}. */
    private Intent mIntentPartnerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MessageUtil.setThoughtSent(this, false);

        mIntentPartnerActivity = new Intent(SplashActivity.this, PartnerActivity.class);

        if (ParseUser.getCurrentUser() != null)
        {
            startActivity(mIntentPartnerActivity);
            finish();
            return;
        }
        else if (AccountUtil.doesAccountExist(this))
        {
            login(null);
        }

        if (savedInstanceState == null)
        {
            // Fragment has not been created yet
            RegisterFragment fragment = RegisterFragment.newInstance(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, fragment)
                    .commit();
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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

    @Override
    public void showProgressBar(int message)
    {
        if (mLinearLayoutProgress == null)
            mLinearLayoutProgress = (LinearLayout) findViewById(R.id.ll_progress);
        if (mTextViewProgress == null)
            mTextViewProgress = (TextView) findViewById(R.id.tv_progress);
        if (mFrameLayoutContainer == null)
            mFrameLayoutContainer = (FrameLayout) findViewById(R.id.login_container);

        mTextViewProgress.setText(message);
        mLinearLayoutProgress.setVisibility(View.VISIBLE);
        mFrameLayoutContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar()
    {
        mLinearLayoutProgress.setVisibility(View.GONE);
        mFrameLayoutContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Logs a user into Parse in the background.
     */
    private final class LoginTask
            extends AsyncTask<RegisterFragment.LoginCallback, Void, Integer>
    {

        /** Instance of callback interface for result. */
        private RegisterFragment.LoginCallback mCallback;
        /** Username used to log in. */
        private String mUsername;

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
                    PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
            mUsername = preferences.getString(AccountUtil.USERNAME, null);
            final String accountPass = preferences.getString(AccountUtil.PASSWORD, null);

            if (mUsername == null || accountPass == null)
            {
                return ParseException.USERNAME_MISSING;
            }

            try
            {
                ParseUser.logIn(mUsername, accountPass);
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
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("username", mUsername);
                    installation.saveInBackground();
                    startActivity(mIntentPartnerActivity);
                    finish();
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorDialog(SplashActivity.this, "Connection failed",
                            "Failed to connect to the server. Please, try again. If this error"
                                    + "persists, your connection may not be sufficient.");
                    break;
                case ParseException.OBJECT_NOT_FOUND:
                    ErrorUtil.displayErrorDialog(SplashActivity.this, "Incorrect credentials",
                            "Your account is no longer valid. Please, create another.");
                    AccountUtil.deleteAccount(SplashActivity.this, null);
                    break;
                case ParseException.USERNAME_MISSING:
                    // does nothing
                    break;
                default:
                    ErrorUtil.displayErrorDialog(SplashActivity.this, "Error",
                            "An error has occurred. Please, try again.");
            }
        }
    }
}
