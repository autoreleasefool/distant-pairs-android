package ca.josephroque.partners.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.josephroque.partners.FullscreenActivity;
import ca.josephroque.partners.R;

/**
 * A simple {@link Fragment} subclass. Use the {@link ThoughtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThoughtFragment
        extends Fragment
        implements FullscreenActivity.MessageDisplay
{

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment ThoughtFragment.
     */
    public static ThoughtFragment newInstance()
    {
        return new ThoughtFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_thought, container, false);
        return rootView;
    }

    @Override
    public void displayMessage(String message)
    {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .show();
        // TODO: display message
    }
}
