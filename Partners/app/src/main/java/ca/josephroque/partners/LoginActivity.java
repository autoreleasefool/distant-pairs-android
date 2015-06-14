package ca.josephroque.partners;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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

    /** To display login and registration progress. */
    private ProgressBar mProgressBarLogin;
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

        mProgressBarLogin = (ProgressBar) findViewById(R.id.pb_login);
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
            mProgressBarLogin.setIndeterminate(true);
            mProgressBarLogin.setVisibility(View.VISIBLE);
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

            return AccountUtil.ACCOUNT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            switch (result)
            {
                case AccountUtil.ACCOUNT_SUCCESS:
                    startActivity(mFullscreenIntent);
                    finish();
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
