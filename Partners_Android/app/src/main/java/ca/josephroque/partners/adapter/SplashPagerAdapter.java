package ca.josephroque.partners.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ca.josephroque.partners.fragment.RegisterFragment;
import ca.josephroque.partners.fragment.TutorialFragment;

/**
 * Created by Joseph Roque on 2015-07-03.
 *
 * Manages fragments in a view pager.
 */
public class SplashPagerAdapter
        extends FragmentStatePagerAdapter
{

    /**
     * Default constructor.
     *
     * @param fm fragment manager
     */
    public SplashPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        if (position < TutorialFragment.TUTORIAL_PAGES)
            return TutorialFragment.newInstance(position);
        else
            return RegisterFragment.newInstance(true);
    }

    @Override
    public int getCount()
    {
        return TutorialFragment.TUTORIAL_PAGES + 1;
    }
}
