package ca.josephroque.partners;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import ca.josephroque.partners.adapter.SplashPagerAdapter;
import ca.josephroque.partners.fragment.RegisterFragment;
import ca.josephroque.partners.fragment.TutorialFragment;
import ca.josephroque.partners.util.AccountUtils;
import ca.josephroque.partners.util.ErrorUtils;
import ca.josephroque.partners.util.MessageUtils;

/**
 * Provides interface for user registration and login.
 */
public class SplashActivity
        extends ProgressActivity
        implements RegisterFragment.RegisterCallbacks
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "SplashActivity";

    /** Alpha value for an active indicator dot. */
    private static final float INDICATOR_ACTIVE = 0.75f;
    /** Alpha value for an inactive indicator dot. */
    private static final float INDICATOR_INACTIVE = 0.25f;

    /** Displays progress when connecting to server. */
    private LinearLayout mLinearLayoutProgress;
    /** Displays action when connecting to server. */
    private TextView mTextViewProgress;
    /** View pager for content fragments. */
    private ViewPager mViewPagerContent;
    /** Toolbar associated with view pager. */
    private LinearLayout mLinearLayoutToolbar;

    /** Intent to initiate instance of {@link PartnerActivity}. */
    private Intent mIntentPartnerActivity;

    /** Current page of view pager. */
    private int mCurrentTutorialPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MessageUtils.setThoughtSent(this, false);
        MessageUtils.setStatusSent(this, false);

        mIntentPartnerActivity = new Intent(SplashActivity.this, PartnerActivity.class);

        if (ParseUser.getCurrentUser() != null)
        {
            startActivity(mIntentPartnerActivity);
            finish();
            return;
        }
        else if (AccountUtils.doesAccountExist(this))
        {
            login(null);
        }

        setupViewPager();

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

        mTextViewProgress.setText(message);
        mLinearLayoutProgress.setVisibility(View.VISIBLE);
        mViewPagerContent.setVisibility(View.INVISIBLE);
        mLinearLayoutToolbar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideProgressBar()
    {
        mLinearLayoutProgress.setVisibility(View.GONE);
        mViewPagerContent.setVisibility(View.VISIBLE);
        if (mViewPagerContent.getCurrentItem() < TutorialFragment.TUTORIAL_PAGES - 1)
            mLinearLayoutToolbar.setVisibility(View.VISIBLE);
    }

    /**
     * Gets adapter for view pager and initializes views.
     */
    private void setupViewPager()
    {
        mViewPagerContent = (ViewPager) findViewById(R.id.splash_view_pager);
        final SplashPagerAdapter adapter = new SplashPagerAdapter(getSupportFragmentManager());
        mViewPagerContent.setAdapter(adapter);
        mLinearLayoutToolbar = (LinearLayout) findViewById(R.id.ll_splash_toolbar);

        final View[] positionIndicator = new View[TutorialFragment.TUTORIAL_PAGES + 1];
        for (int i = 0; i < positionIndicator.length; i++)
        {
            final int viewId = getResources().getIdentifier("view_indicator_" + i, "id",
                    PartnersApplication.getSimplePackageName());
            positionIndicator[i] = mLinearLayoutToolbar.findViewById(viewId);
            positionIndicator[i].setAlpha(INDICATOR_INACTIVE);
        }
        positionIndicator[0].setAlpha(INDICATOR_ACTIVE);

        mViewPagerContent.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                if (position == TutorialFragment.TUTORIAL_PAGES)
                    mLinearLayoutToolbar.setVisibility(View.INVISIBLE);
                else
                    mLinearLayoutToolbar.setVisibility(View.VISIBLE);

                //Changes which page indicator is 'highlighted'
                positionIndicator[mCurrentTutorialPage].setAlpha(INDICATOR_INACTIVE);
                positionIndicator[position].setAlpha(INDICATOR_ACTIVE);

                mCurrentTutorialPage = position;

                Fragment fragment = adapter.getRegisteredFragment(mCurrentTutorialPage);
                if (fragment instanceof TutorialFragment)
                    ((TutorialFragment) fragment).startAnimation();
            }
        });
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
            mUsername = preferences.getString(AccountUtils.USERNAME, null);
            final String accountPass = preferences.getString(AccountUtils.PASSWORD, null);

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

            return AccountUtils.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            hideProgressBar();

            if (mCallback != null && result != AccountUtils.SUCCESS)
                mCallback.onLoginFailed(result);

            switch (result)
            {
                case AccountUtils.SUCCESS:
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("username", mUsername);
                    installation.saveInBackground();
                    startActivity(mIntentPartnerActivity);
                    finish();
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtils.displayErrorDialog(SplashActivity.this, "Connection failed",
                            "Failed to connect to the server. Please, try again. If this error"
                                    + "persists, your connection may not be sufficient.");
                    break;
                case ParseException.OBJECT_NOT_FOUND:
                    ErrorUtils.displayErrorDialog(SplashActivity.this, "Incorrect credentials",
                            "Your account is no longer valid. Please, create another.");
                    AccountUtils.deleteAccount(SplashActivity.this, null);
                    break;
                case ParseException.USERNAME_MISSING:
                    // does nothing
                    break;
                default:
                    ErrorUtils.displayErrorDialog(SplashActivity.this, "Error",
                            "An error has occurred. Please, try again.");
            }
        }
    }
}
