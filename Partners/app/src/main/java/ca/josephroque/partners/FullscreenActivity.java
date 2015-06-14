package ca.josephroque.partners;

import ca.josephroque.partners.util.hider.SystemUiHider;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;


/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity
        extends FragmentActivity
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
        startService(new Intent(FullscreenActivity.this, MessageService.class));

        final View contentView = findViewById(R.id.fullscreen_content);

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
        mViewPager = (ViewPager) findViewById(R.id.fullscreen_content);
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
            return null;
        }

        @Override
        public int getCount()
        {
            return 2;
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
}
