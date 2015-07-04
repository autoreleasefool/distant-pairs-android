package ca.josephroque.partners.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.josephroque.partners.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialFragment
        extends Fragment
{

    public static final int TUTORIAL_PAGES = 5;

    /** Represents page of the tutorial displayed by this fragment. */
    private static final String ARG_PAGE = "arg_page";

    /** Page of the tutorial shown in this fragment. */
    private int mTutorialPage;

    /**
     * Creates a new instance of TutorialFragment.
     *
     * @param page page of tutorial
     * @return new instance
     */
    public static TutorialFragment newInstance(int page)
    {
        TutorialFragment instance = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        instance.setArguments(args);
        return instance;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);

        if (savedInstanceState == null)
            mTutorialPage = getArguments().getInt(ARG_PAGE);
        else
            mTutorialPage = savedInstanceState.getInt(ARG_PAGE);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_PAGE, mTutorialPage);
    }
}
