package ca.josephroque.partners.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Joseph Roque on 2015-07-06. Offers methods and constants to access preferences.
 */
public final class PreferenceUtils
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "PreferencesUtils";

    /** Preference for rating. */
    public static final String PREF_RATE = "pref_rate";
    /** Preference for reporting a bug. */
    public static final String PREF_REPORT_BUG = "pref_report_bug";
    /** Preference for sending a comment or suggestion. */
    public static final String PREF_COMMENT_SUGGESTION = "pref_comment_suggestion";

    /** Preference for enabling or disabling the pulsing heart animation. */
    public static final String PREF_ENABLE_PULSE = "pref_enable_pulse";
    /** Preference for enabling or disabling the super cute heart animation. */
    public static final String PREF_ENABLE_SUPER_CUTE_HEART = "pref_enable_super_cute_hearts";
    /** Preference for enabling or disabling the prompt to send thought when partner is offline. */
    public static final String PREF_ENABLE_THOUGHT_PROMPT = "pref_enable_thought_prompt";
    /** Preference to delete the user account. */
    public static final String PREF_DELETE_ACCOUNT = "pref_delete_account";
    /** Preference to delete the user's pair. */
    public static final String PREF_DELETE_PAIR = "pref_delete_pair";

    /**
     * Default private constructor.
     */
    private PreferenceUtils()
    {
        // does nothing
    }
}
