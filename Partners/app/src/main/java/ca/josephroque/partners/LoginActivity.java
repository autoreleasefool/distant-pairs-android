package ca.josephroque.partners;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;

/**
 * Registering and logging in user.
 */
public class LoginActivity
        extends Activity
{

    /** To display login and registation status. */
    private RelativeLayout mRelativeLayoutLogin;
    /** To display login and registration status. */
    private TextView mTextViewLogin;
    /** For user input for username. */
    private EditText mEditTextUsername;
    /** Registers user when username is entered. */
    private Button mButtonRegister;

    /** Intent to begin FullscreenActivity. */
    private Intent mFullscreenIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFullscreenIntent = new Intent(LoginActivity.this, FullscreenActivity.class);

        if (ParseUser.getCurrentUser() != null)
        {
            startActivity(mFullscreenIntent);
            finish();
        }

        ProgressBar progressBarLogin = (ProgressBar) findViewById(R.id.pb_login_register);
        progressBarLogin.setIndeterminate(true);

        mRelativeLayoutLogin = (RelativeLayout) findViewById(R.id.rl_login_register);
        mTextViewLogin = (TextView) findViewById(R.id.tv_login_register);
        mEditTextUsername = (EditText) findViewById(R.id.et_username);
        mButtonRegister = (Button) findViewById(R.id.btn_register);

        mButtonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String accountName = mEditTextUsername.getText().toString().trim();
                accountName = accountName.replaceAll("\\s+", " ");
                if (!accountName.matches("^[a-zA-Z0-9 ]+$"))
                {
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Invalid username!",
                            "Your username can only contain letters, numbers, and spaces.");
                    return;
                }

                new RegisterAccountTask().execute(accountName,
                        AccountUtil.randomAlphaNumericPassword());
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        login(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Enables or disables views for logging in.
     *
     * @param enabled if true, views are enabled
     */
    private void setLoginEnabled(boolean enabled)
    {
        mButtonRegister.setEnabled(enabled);
        mEditTextUsername.setEnabled(enabled);
        if (enabled)
            mRelativeLayoutLogin.setVisibility(View.GONE);
    }

    /**
     * Logs user into server.
     * @param showErrorMessage if true, shows an error message if no account exists
     */
    private void login(boolean showErrorMessage)
    {
        mRelativeLayoutLogin.setVisibility(View.VISIBLE);
        mTextViewLogin.setText(R.string.text_logging_in);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String accountUsername = preferences.getString(AccountUtil.USERNAME, null);
        final String accountPassword = preferences.getString(AccountUtil.PASSWORD, null);

        if (accountUsername == null || accountPassword == null)
        {
            setLoginEnabled(true);
            if (!showErrorMessage)
                return;

            ErrorUtil.displayErrorMessage(this, "Account unavailable",
                    "Unable to access account credentials. You will need to create a new account.");
            return;
        }

        ParseUser.logInInBackground(accountUsername, accountPassword, new LogInCallback()
        {
            @Override
            public void done(ParseUser parseUser, ParseException e)
            {
                if (parseUser != null)
                {
                    startActivity(mFullscreenIntent);
                    finish();
                }
                else
                {
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Account unavailable",
                            "Unable to access account credentials. You will need to create a "
                                    + "new account.");
                    setLoginEnabled(true);
                }
            }
        });
    }

    /**
     * Registers new account in background.
     */
    private final class RegisterAccountTask
            extends AsyncTask<String, Void, Integer>
    {

        @Override
        protected void onPreExecute()
        {
            mRelativeLayoutLogin.setVisibility(View.VISIBLE);
            mTextViewLogin.setText(R.string.text_registering);
            setLoginEnabled(false);
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

            AccountUtil.saveAccountCredentials(LoginActivity.this, credentials[0], credentials[1]);
            return AccountUtil.ACCOUNT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            switch (result)
            {
                case AccountUtil.ACCOUNT_SUCCESS:
                    login(true);
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Username taken",
                            "That username has already been taken. Please, try another.");
                    setLoginEnabled(true);
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    setLoginEnabled(true);
                    break;
                default:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Error registering",
                            "Unable to register your account. Please, try again.");
                    setLoginEnabled(true);
                    break;
            }
        }
    }
}
