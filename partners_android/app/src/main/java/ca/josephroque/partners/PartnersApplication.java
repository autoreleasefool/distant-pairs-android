package ca.josephroque.partners;

import android.app.Application;
import android.preference.PreferenceManager;

import com.parse.Parse;

import ca.josephroque.partners.database.DBHelper;

/**
 * Created by Joseph Roque on 2015-06-18. To initialize libraries and objects before app begins.
 */
public class PartnersApplication
        extends Application
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "PartnersApplication";

    /** Name of application package. */
    private static String sPackageName;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sPackageName = getPackageName();

        // Creates database if it doesn't exist
        DBHelper.getInstance(this);

        // Sets default preference values
        PreferenceManager.setDefaultValues(this, R.xml.pref_other, false);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "jWG1xELtom5AqG1jrv7swNBg3wgWlLLAAhfzhfM7",
                "3PijZadO6sQXu4ZazsxjtGTyNfddeQ59jLAl1CCq");
    }

    /**
     * Returns package name of the application.
     *
     * @return {@code sPackageName}
     */
    public static String getSimplePackageName()
    {
        return sPackageName;
    }
}
