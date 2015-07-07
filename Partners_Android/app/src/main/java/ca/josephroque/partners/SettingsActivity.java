package ca.josephroque.partners;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.support.v4.content.LocalBroadcastManager;

import ca.josephroque.partners.util.AccountUtils;
import ca.josephroque.partners.util.EmailUtils;
import ca.josephroque.partners.util.ErrorUtils;
import ca.josephroque.partners.util.PreferenceUtils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings.
 */
@SuppressWarnings("deprecation")
public class SettingsActivity
        extends PreferenceActivity
        implements Preference.OnPreferenceClickListener
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
        setPreferencesEnabled(true);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the device configuration
     * dictates that a simplified, single-pane UI should be shown.
     */
    private void setupSimplePreferencesScreen()
    {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_other);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_other);

        findPreference(PreferenceUtils.PREF_RATE).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_REPORT_BUG).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_COMMENT_SUGGESTION).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_DELETE_ACCOUNT).setOnPreferenceClickListener(this);
        findPreference(PreferenceUtils.PREF_DELETE_PAIR).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (PreferenceUtils.PREF_RATE.equals(preference.getKey()))
        {
            //Opens Google Play or chrome to display app
            final String appPackageName = getPackageName();
            try
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName)));
            }
            catch (android.content.ActivityNotFoundException ex)
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id="
                                + appPackageName)));
            }
            return true;
        }
        else if (PreferenceUtils.PREF_REPORT_BUG.equals(preference.getKey()))
        {
            String emailBody =
                    "Please try to include as much of the following information as possible:"
                            + "\nWhere in the application the bug occurred,"
                            + "\nWhat you were doing when the bug occurred,"
                            + "\nThe nature of the bug - fatal, minor, cosmetic (the way the app looks)"
                            + "\n\n";

            Intent emailIntent = EmailUtils.getEmailIntent(
                    "bugs@josephroque.ca",
                    "Bug: " + getResources().getString(R.string.app_name),
                    emailBody);
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            return true;
        }
        else if (PreferenceUtils.PREF_COMMENT_SUGGESTION.equals(preference.getKey()))
        {
            Intent emailIntent = EmailUtils.getEmailIntent(
                    "contact@josephroque.ca",
                    "Comm/Sug: " + getResources().getString(R.string.app_name),
                    null);
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            return true;
        }
        else if (PreferenceUtils.PREF_DELETE_ACCOUNT.equals(preference.getKey()))
        {
            AccountUtils.promptDeleteAccount(this, new AccountUtils.DeleteAccountCallback()
            {
                @Override
                public void onDeleteAccountStarted()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setPreferencesEnabled(false);
                        }
                    });
                }

                @Override
                public void onDeleteAccountEnded()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            LocalBroadcastManager.getInstance(SettingsActivity.this)
                                    .sendBroadcast(new Intent(PartnerActivity.ACTION_FINISH));
                            Intent loginIntent =
                                    new Intent(SettingsActivity.this, SplashActivity.class);
                            startActivity(loginIntent);
                            finish();
                        }
                    });
                }

                @Override
                public void onDeleteAccountError(String message)
                {
                    if (message != null)
                        ErrorUtils.displayErrorDialog(SettingsActivity.this,
                                "Error deleting account", message);
                    else
                        ErrorUtils.displayErrorDialog(SettingsActivity.this,
                                "Error deleting account", "An unknown error occurred and you"
                                        + " may not be able to use this username again.");
                    onDeleteAccountEnded();
                }
            });
        }
        else if (PreferenceUtils.PREF_DELETE_PAIR.equals(preference.getKey()))
        {
            AccountUtils.promptDeletePair(this, new AccountUtils.DeleteAccountCallback()
            {
                @Override
                public void onDeleteAccountStarted()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setPreferencesEnabled(false);
                        }
                    });
                }

                @Override
                public void onDeleteAccountEnded()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            LocalBroadcastManager.getInstance(SettingsActivity.this)
                                    .sendBroadcast(new Intent(PartnerActivity.ACTION_FINISH));
                            Intent partnerIntent =
                                    new Intent(SettingsActivity.this, PartnerActivity.class);
                            startActivity(partnerIntent);
                            finish();
                        }
                    });
                }

                @Override
                public void onDeleteAccountError(String message)
                {
                    if (message != null)
                        ErrorUtils.displayErrorDialog(SettingsActivity.this,
                                "Error deleting pair", message);
                    else
                        ErrorUtils.displayErrorDialog(SettingsActivity.this,
                                "Error deleting pair", "An unknown error occurred.");
                    onDeleteAccountEnded();
                }
            });
        }

        return false;
    }

    /**
     * Enables or disables all preferences.
     *
     * @param enabled true to enable all preferences, false to disable
     */
    private void setPreferencesEnabled(boolean enabled)
    {
        findPreference(PreferenceUtils.PREF_RATE).setEnabled(enabled);
        findPreference(PreferenceUtils.PREF_REPORT_BUG).setEnabled(enabled);
        findPreference(PreferenceUtils.PREF_COMMENT_SUGGESTION).setEnabled(enabled);
        findPreference(PreferenceUtils.PREF_ENABLE_PULSE).setEnabled(enabled);
        findPreference(PreferenceUtils.PREF_ENABLE_THOUGHT_PROMPT).setEnabled(enabled);
        findPreference(PreferenceUtils.PREF_DELETE_ACCOUNT).setEnabled(enabled);
        findPreference(PreferenceUtils.PREF_DELETE_PAIR).setEnabled(enabled);
    }
}
