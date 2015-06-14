package ca.josephroque.partners;

import android.annotation.SuppressLint;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;

/**
 * Registering and logging in user.
 */
public class LoginActivity
        extends Activity
        implements View.OnClickListener
{

    /** To display login and registation status. */
    private RelativeLayout mRelativeLayoutLogin;
    /** To display login and registration status. */
    private TextView mTextViewLogin;
    /** For user input for username. */
    private EditText mEditTextUsername;
    /** Registers user when username is entered. */
    private Button mButtonRegister;
    /** Checks server for requests to be partnered with this user. */
    private Button mButtonPairCheck;

    /** Indicates if user is selecting a partner. */
    private boolean mSelectingPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ProgressBar progressBarLogin = (ProgressBar) findViewById(R.id.pb_login_register);
        progressBarLogin.setIndeterminate(true);

        mRelativeLayoutLogin = (RelativeLayout) findViewById(R.id.rl_login_register);
        mTextViewLogin = (TextView) findViewById(R.id.tv_login_register);
        mEditTextUsername = (EditText) findViewById(R.id.et_username);
        mButtonRegister = (Button) findViewById(R.id.btn_register);
        mButtonPairCheck = (Button) findViewById(R.id.btn_pair_check);

        mButtonRegister.setOnClickListener(this);
        mButtonPairCheck.setOnClickListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        ParseUser.logOutInBackground();
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

    @Override
    public void onClick(View src)
    {
        switch (src.getId())
        {
            case R.id.btn_register:
                if (!mSelectingPartner)
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
                else
                {
                    String accountName = PreferenceManager.getDefaultSharedPreferences(
                            LoginActivity.this).getString(AccountUtil.USERNAME, null);
                    String partnerName = mEditTextUsername.getText().toString().trim();
                    partnerName = partnerName.replaceAll("\\s+", " ");
                    if (!partnerName.matches("^[a-zA-Z0-9 ]+$"))
                    {
                        ErrorUtil.displayErrorMessage(LoginActivity.this, "Invalid username!",
                                "Your pair's name can only contain letters, numbers, and spaces.");
                        return;
                    }
                    else if (partnerName.equalsIgnoreCase(accountName))
                    {
                        ErrorUtil.displayErrorMessage(LoginActivity.this, "Invalid username!",
                                "You cannot be your own pair.");
                        return;
                    }

                    new RegisterPartnerTask().execute(partnerName);
                }
                break;
            case R.id.btn_pair_check:
                // TODO: check for pair requests
                break;
            default:
                // does nothing
        }
    }

    /**
     * Enables or disables views for logging in.
     *
     * @param enabled if true, views are enabled
     */
    private void setLoginEnabled(boolean enabled)
    {
        mSelectingPartner = false;

        mButtonRegister.setText(R.string.text_register);
        mEditTextUsername.setText("");
        mButtonPairCheck.setVisibility(View.GONE);

        mButtonRegister.setEnabled(enabled);
        mEditTextUsername.setEnabled(enabled);
        if (enabled)
            mRelativeLayoutLogin.setVisibility(View.GONE);
    }

    /**
     * Changes content to let user select a partner.
     *
     * @param enabled if true, views are enabled
     */
    private void setPartnerSelectEnabled(boolean enabled)
    {
        mSelectingPartner = true;

        mButtonRegister.setText(R.string.text_set_pair);
        mEditTextUsername.setText("");
        mButtonPairCheck.setVisibility(View.VISIBLE);

        mEditTextUsername.setEnabled(enabled);
        mButtonRegister.setEnabled(enabled);
        mButtonPairCheck.setEnabled(enabled);

        if (enabled)
            mRelativeLayoutLogin.setVisibility(View.GONE);
    }

    /**
     * Logs user into server.
     *
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

        ParseUser.logInInBackground(accountUsername.toLowerCase(), accountPassword,
                new LogInCallback()
                {
                    @Override
                    public void done(ParseUser parseUser, ParseException e)
                    {
                        if (parseUser != null)
                        {
                            if (AccountUtil.doesPartnerExist(LoginActivity.this))
                                beginInteraction();
                            else
                                setPartnerSelectEnabled(true);
                        }
                        else
                        {
                            ErrorUtil.displayErrorMessage(LoginActivity.this, "Account unavailable",
                                    "Unable to access account credentials. You will need to create"
                                            + " a new account.");
                            setLoginEnabled(true);
                        }
                    }
                });
    }

    /**
     * Begins user interactions with a partner.
     */
    private void beginInteraction()
    {
        Intent fullscreenIntent = new Intent(LoginActivity.this, FullscreenActivity.class);
        startActivity(fullscreenIntent);
        finish();
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
            parseUser.setUsername(credentials[0].toLowerCase());
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

    /**
     * Registers account with a pair in the background.
     */
    private final class RegisterPartnerTask
            extends AsyncTask<String, Void, Integer>
    {

        @Override
        protected void onPreExecute()
        {
            mRelativeLayoutLogin.setVisibility(View.VISIBLE);
            mTextViewLogin.setText(R.string.text_registering_partner);
            setPartnerSelectEnabled(false);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        protected Integer doInBackground(String... partnerName)
        {
            ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
            parseQuery.whereEqualTo("username", partnerName[0].toLowerCase());

            try
            {
                List<ParseUser> result = parseQuery.find();
                if (result.size() == 0)
                    return ParseException.USERNAME_MISSING;

                ParseQuery<ParseObject> pairQuery = new ParseQuery<>("Pair");
                pairQuery.whereEqualTo(AccountUtil.PAIR, partnerName[0]);
                List<ParseObject> pairResult = pairQuery.find();

                if (pairResult.size() > 0)
                    return ParseException.USERNAME_TAKEN;

                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                String accountUsername = preferences.getString(AccountUtil.USERNAME, null);

                ParseObject parseObject = new ParseObject("Pair");
                parseObject.put(AccountUtil.USERNAME, accountUsername);
                parseObject.put(AccountUtil.PAIR, partnerName[0]);
                parseObject.save();

                preferences.edit()
                        .putString(AccountUtil.PARSE_PAIR_ID, parseObject.getObjectId())
                        .commit();

                AccountUtil.savePairCredentials(LoginActivity.this, partnerName[0]);
                return AccountUtil.ACCOUNT_SUCCESS;
            }
            catch (ParseException ex)
            {
                return ex.getCode();
            }
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            switch (result)
            {
                case AccountUtil.ACCOUNT_SUCCESS:
                    beginInteraction();
                    return;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    break;
                case ParseException.USERNAME_MISSING:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Error registering pair",
                            "This user does not exist. Please, try again.");
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Error registering pair",
                            "This user already has a pair. Please, try again.");
                    break;
                default:
                    ErrorUtil.displayErrorMessage(LoginActivity.this, "Error registering pair",
                            "This user may not exist or may already be paired. Please, try again.");
                    break;
            }
            setPartnerSelectEnabled(true);
        }
    }
}
