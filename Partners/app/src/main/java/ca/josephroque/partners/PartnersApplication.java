package ca.josephroque.partners;

import android.app.Application;

import com.parse.Parse;

import ca.josephroque.partners.database.DBHelper;

/**
 * Created by Joseph Roque on 2015-06-18.
 *
 * To initialize libraries and objects before app begins.
 */
public class PartnersApplication extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Creates database if it doesn't exist
        DBHelper.getInstance(this);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "jWG1xELtom5AqG1jrv7swNBg3wgWlLLAAhfzhfM7",
                "3PijZadO6sQXu4ZazsxjtGTyNfddeQ59jLAl1CCq");
    }
}
