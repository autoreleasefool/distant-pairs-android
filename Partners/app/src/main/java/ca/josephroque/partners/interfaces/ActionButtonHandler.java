package ca.josephroque.partners.interfaces;

/**
 * Created by Joseph Roque on 2015-06-19.
 *
 * Provides methods which fragments must override to handle floating action button clicks.
 */
public interface ActionButtonHandler
{

    /**
     * Invoked when a floating action button is clicked.
     */
    void handleActionClick();
}
