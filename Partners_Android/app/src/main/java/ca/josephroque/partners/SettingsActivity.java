package ca.josephroque.partners;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import ca.josephroque.partners.util.AccountUtils;
import ca.josephroque.partners.util.EmailUtils;
import ca.josephroque.partners.util.ErrorUtils;
import ca.josephroque.partners.util.PreferenceUtils;
import ca.josephroque.partners.util.WatchUtils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings.
 */
public class SettingsActivity
        extends PreferenceActivity
        implements Preference.OnPreferenceClickListener
{

    /** Indicates if the preferences should provide on click interactions. */
    private static boolean sPreferencesEnabled = true;

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the device configuration
     * dictates that a simplified, single-pane UI should be shown.
     */
    @SuppressWarnings("deprecation")    // must be used for single pane UIs
    private void setupSimplePreferencesScreen()
    {
        if (!isSimplePreferences(this))
        {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'watch' preferences, and a corresponding header.
        PreferenceCategory header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_watch);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_watch);

        // Add 'other' preferences, and a corresponding header.
        header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_other);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_other);

        findPreference(PreferenceUtils.PREF_ADD_WATCH).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_DELETE_WATCH).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_RATE).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_REPORT_BUG).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_COMMENT_SUGGESTION).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_DELETE_ACCOUNT).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_DELETE_PAIR).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (!sPreferencesEnabled)
            return false;

        if (PreferenceUtils.PREF_DELETE_ACCOUNT.equals(preference.getKey()))
        {
            deleteAccount(SettingsActivity.this);
            return true;
        }
        else if (PreferenceUtils.PREF_DELETE_PAIR.equals(preference.getKey()))
        {
            deletePair(SettingsActivity.this);
            return true;
        }
        else if (PreferenceUtils.PREF_RATE.equals(preference.getKey()))
        {
            openPlayStore(SettingsActivity.this);
            return true;
        }
        else if (PreferenceUtils.PREF_REPORT_BUG.equals(preference.getKey()))
        {
            emailBug(SettingsActivity.this);
            return true;
        }
        else if (PreferenceUtils.PREF_COMMENT_SUGGESTION.equals(preference.getKey()))
        {
            emailCommentOrSuggestion(SettingsActivity.this);
            return true;
        }
        else if (PreferenceUtils.PREF_ADD_WATCH.equals(preference.getKey()))
        {
            addWatch(SettingsActivity.this);
            return true;
        }
        else if (PreferenceUtils.PREF_DELETE_WATCH.equals(preference.getKey()))
        {
            deleteWatch(SettingsActivity.this);
            return true;
        }
        return false;
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For example, 10" tablets
     * are extra-large.
     *
     * @param context currently running context
     * @return true if the device is an extra large tablet
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is true if  the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device doesn't have an
     * extra-large screen. In these cases, a single-pane "simplified" settings UI should be shown.
     *
     * @param context currently running context
     * @return true if preference fragments should not be used
     */
    private static boolean isSimplePreferences(Context context)
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        if (!isSimplePreferences(this))
        {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private static void deleteAccount(final Activity activity)
    {
        AccountUtils.promptDeleteAccount(activity, new AccountUtils.DeleteAccountCallback()
        {
            @Override
            public void onDeleteAccountStarted()
            {
                // does nothing
            }

            @Override
            public void onDeleteAccountEnded()
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LocalBroadcastManager.getInstance(activity)
                                .sendBroadcast(new Intent(PartnerActivity.ACTION_FINISH));
                        Intent loginIntent =
                                new Intent(activity, SplashActivity.class);
                        activity.startActivity(loginIntent);
                        activity.finish();
                    }
                });
            }

            @Override
            public void onDeleteAccountError(String message)
            {
                if (message != null)
                    ErrorUtils.displayErrorDialog(activity,
                            "Error deleting account", message);
                else
                    ErrorUtils.displayErrorDialog(activity,
                            "Error deleting account", "An unknown error occurred and you"
                                    + " may not be able to use this username again.");
                onDeleteAccountEnded();
            }
        });
    }

    private static void deletePair(final Activity activity)
    {
        AccountUtils.promptDeletePair(activity, new AccountUtils.DeleteAccountCallback()
        {
            @Override
            public void onDeleteAccountStarted()
            {
                // does nothing
            }

            @Override
            public void onDeleteAccountEnded()
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LocalBroadcastManager.getInstance(activity)
                                .sendBroadcast(new Intent(PartnerActivity.ACTION_FINISH));
                        Intent partnerIntent =
                                new Intent(activity, PartnerActivity.class);
                        activity.startActivity(partnerIntent);
                        activity.finish();
                    }
                });
            }

            @Override
            public void onDeleteAccountError(String message)
            {
                if (message != null)
                    ErrorUtils.displayErrorDialog(activity, "Error deleting pair", message);
                else
                    ErrorUtils.displayErrorDialog(activity,
                            "Error deleting pair", "An unknown error occurred.");
                onDeleteAccountEnded();
            }
        });
    }

    private static void openPlayStore(Activity activity)
    {
        //Opens Google Play or chrome to display app
        final String appPackageName = activity.getPackageName();
        try
        {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + appPackageName)));
        }
    }

    private static void emailBug(Activity activity)
    {
        String emailBody =
                "Please try to include as much of the following information as possible:"
                        + "\nWhere in the application the bug occurred,"
                        + "\nWhat you were doing when the bug occurred,"
                        + "\nThe nature of the bug - fatal, minor, cosmetic (the way the app looks)"
                        + "\n\n";

        Intent emailIntent = EmailUtils.getEmailIntent(
                "bugs@josephroque.ca",
                "Bug: " + activity.getResources().getString(R.string.app_name),
                emailBody);
        activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private static void emailCommentOrSuggestion(Activity activity)
    {
        Intent emailIntent = EmailUtils.getEmailIntent(
                "contact@josephroque.ca",
                "Comm/Sug: " + activity.getResources().getString(R.string.app_name),
                null);
        activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    /**
     * Prompts user to add a watch to their account.
     *
     * @param context to create dialog
     */
    private static void addWatch(Context context)
    {
        WatchUtils.addWatch(context);
    }

    /**
     * Prompts user to delete a watch from the user's account.
     *
     * @param context to create dialog
     */
    private static void deleteWatch(Context context)
    {
        WatchUtils.deleteWatch(context);
    }

    /**
     * This fragment shows general preferences only. It is used when the activity is showing a
     * two-pane settings UI.
     */
    public static class GeneralPreferenceFragment
            extends PreferenceFragment
            implements Preference.OnPreferenceClickListener
    {

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            findPreference(PreferenceUtils.PREF_DELETE_ACCOUNT).setOnPreferenceClickListener(this);
            findPreference(PreferenceUtils.PREF_DELETE_PAIR).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            if (!sPreferencesEnabled)
                return false;

            if (PreferenceUtils.PREF_DELETE_ACCOUNT.equals(preference.getKey()))
            {
                deleteAccount(getActivity());
                return true;
            }
            else if (PreferenceUtils.PREF_DELETE_PAIR.equals(preference.getKey()))
            {
                deletePair(getActivity());
                return true;
            }
            return false;
        }
    }

    /**
     * This fragment shows watch preferences only. It is used when the activity is showing a
     * two-pane settings UI.
     */
    public static class WatchPreferenceFragment
            extends PreferenceFragment
            implements Preference.OnPreferenceClickListener
    {

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_watch);

            findPreference(PreferenceUtils.PREF_ADD_WATCH).setOnPreferenceClickListener(this);
            findPreference(PreferenceUtils.PREF_DELETE_WATCH).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            if (!sPreferencesEnabled)
                return false;

            if (PreferenceUtils.PREF_ADD_WATCH.equals(preference.getKey()))
            {
                addWatch(getActivity());
                return true;
            }
            else if (PreferenceUtils.PREF_DELETE_WATCH.equals(preference.getKey()))
            {
                deleteWatch(getActivity());
                return true;
            }
            return false;
        }
    }

    /**
     * This fragment shows other preferences only. It is used when the activity is showing a
     * two-pane settings UI.
     */
    public static class OtherPreferenceFragment
            extends PreferenceFragment
            implements Preference.OnPreferenceClickListener
    {

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_other);

            findPreference(PreferenceUtils.PREF_RATE).setOnPreferenceClickListener(this);
            findPreference(PreferenceUtils.PREF_REPORT_BUG).setOnPreferenceClickListener(this);
            findPreference(PreferenceUtils.PREF_COMMENT_SUGGESTION).setOnPreferenceClickListener(
                    this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            if (!sPreferencesEnabled)
                return false;

            if (PreferenceUtils.PREF_RATE.equals(preference.getKey()))
            {
                openPlayStore(getActivity());
                return true;
            }
            else if (PreferenceUtils.PREF_REPORT_BUG.equals(preference.getKey()))
            {
                emailBug(getActivity());
                return true;
            }
            else if (PreferenceUtils.PREF_COMMENT_SUGGESTION.equals(preference.getKey()))
            {
                emailCommentOrSuggestion(getActivity());
                return true;
            }
            return false;
        }
    }
}
