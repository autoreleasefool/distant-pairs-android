package ca.josephroque.partners;

import ca.josephroque.partners.fragment.LoginFragment;
import ca.josephroque.partners.util.AccountUtil;
import ca.josephroque.partners.util.ErrorUtil;
import ca.josephroque.partners.util.hider.SystemUiHider;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;


/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity
        extends FragmentActivity
        implements LoginFragment.LoginCallbacks
{

    /**
     * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS}
     * milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction
     * before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /** Initial time before UI is hidden. */
    private static final byte INITIAL_HIDE_DELAY = 100;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the
     * system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /** Manages fragments displayed in the activity. */
    private ViewPager mViewPager;
    /** Manages fragments in view pager. */
    private FullscreenPagerAdapter mPagerAdapter;
    /** Displays login progress. */
    private ProgressBar mProgressBarRegistration;

    /** Current fragment  of view pager. */
    private int mCurrentFragment = 0;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        final View contentView = findViewById(R.id.fullscreen_content);
        mProgressBarRegistration = (ProgressBar) findViewById(R.id.fullscreen_progressbar);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener()
                {
                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible)
                    {
                        if (visible && AUTO_HIDE)
                        {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (TOGGLE_ON_CLICK)
                    mSystemUiHider.toggle();
                else
                    mSystemUiHider.show();
            }
        });

        setupViewPager();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(INITIAL_HIDE_DELAY);
    }

    @Override
    public void registerAccount(final String accountName)
    {
        new RegisterAccountTask().execute(accountName, AccountUtil.randomAlphaNumericPassword());
    }

    /** For posting tasks to hide the UI. */
    private Handler mHideHandler = new Handler();
    /** Hides the UI. */
    private Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled
     * calls.
     *
     * @param delayMillis time before hiding UI
     */
    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Sets up view pager to manage fragments in the activity.
     */
    private void setupViewPager()
    {
        mViewPager = (ViewPager) findViewById(R.id.fullscreen_viewpager);
        mPagerAdapter = new FullscreenPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                mCurrentFragment = position;
            }
        });
    }

    /**
     * Returns true if the user has successfully been logged in or false otherwise.
     *
     * @return true if user is logged in, false otherwise
     */
    private boolean isLoggedIn()
    {
        return false;
    }

    /**
     * Enables login elements of the application.
     */
    private void enableLogin()
    {
        if (isLoggedIn() || mCurrentFragment != 0)
            throw new IllegalStateException(
                    "cannot be called because login fragment is not available");
        ((LoginFragment) mPagerAdapter.getCurrentFragment()).setViewsEnabled(true);
    }

    /**
     * Logs into a user's stored account.
     */
    private void login()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accountName = prefs.getString(AccountUtil.USERNAME, null);
        String accountPassword = prefs.getString(AccountUtil.PASSWORD, null);

        if (accountName == null || accountPassword == null)
        {
            ErrorUtil.displayErrorMessage(this, "Account unavailable",
                    "Could not retrieve account credentials. Please create a new account.");
            enableLogin();
            return;
        }

        ParseUser.logInInBackground(accountName, accountPassword, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e)
            {
                if (parseUser != null)
                {
                }
                else
                {
                    ErrorUtil.displayErrorMessage(FullscreenActivity.this, "Account unavailable",
                            "Could not retrieve account credentials. Please create a new account.");
                    enableLogin();
                }
            }
        });
    }

    /**
     * Manages fragments displayed in the fullscreen activity.
     */
    private final class FullscreenPagerAdapter
            extends FragmentStatePagerAdapter
    {

        /** Array of initiated fragments. */
        private SparseArray<WeakReference<Fragment>> mRegisteredFragments = new SparseArray<>();

        /**
         * Defualt constructor, passes {@code fm} to super constructor.
         *
         * @param fm fragment manager
         */
        private FullscreenPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            Object object = super.instantiateItem(container, position);
            if (object instanceof Fragment)
            {
                mRegisteredFragments.put(position, new WeakReference<>((Fragment) object));
            }
            return object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            super.destroyItem(container, position, object);
            mRegisteredFragments.remove(position);
        }

        @Override
        public Fragment getItem(int position)
        {
            if (isLoggedIn())
            {
                // TODO: return HeartFragment and ThoughtFragment
                return null;
            }
            else
            {
                return LoginFragment.newInstance();
            }
        }

        @Override
        public int getCount()
        {
            if (isLoggedIn())
                return 2;
            else
                return 1;
        }

        /**
         * Gets the current active fragment from {@code registeredFragments}.
         *
         * @return current fragment
         */
        public Fragment getCurrentFragment()
        {
            return mRegisteredFragments.get(mCurrentFragment).get();
        }
    }

    /**
     * Registers a new account in the background.
     */
    private final class RegisterAccountTask
            extends AsyncTask<String, Void, Integer>
    {

        @Override
        protected void onPreExecute()
        {
            mProgressBarRegistration.setIndeterminate(true);
            mProgressBarRegistration.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... accountCredentials)
        {
            ParseUser parseUser = new ParseUser();
            parseUser.setUsername(accountCredentials[0].toLowerCase());
            parseUser.setPassword(accountCredentials[1]);

            try
            {
                parseUser.signUp();
            }
            catch (ParseException ex)
            {
                return ex.getCode();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            mProgressBarRegistration.setVisibility(View.GONE);

            switch (result)
            {
                case AccountUtil.ACCOUNT_SUCCESS:
                    login();
                    break;
                case ParseException.CONNECTION_FAILED:
                    ErrorUtil.displayErrorMessage(FullscreenActivity.this, "Connection failed",
                            "Unable to connect to server. Please, try again.");
                    enableLogin();
                    break;
                case ParseException.USERNAME_TAKEN:
                    ErrorUtil.displayErrorMessage(FullscreenActivity.this, "Username taken",
                            "This username is unavailable. Please, try another.");
                    enableLogin();
                    break;
                default:

            }
        }
    }
}
