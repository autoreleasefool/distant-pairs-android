package ca.josephroque.partners;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Joseph Roque on 2015-06-13.
 * <p/>
 * To initialize object and libraries before application begins.
 */
public class PartnersApplication extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Enable Local Datastore
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "jWG1xELtom5AqG1jrv7swNBg3wgWlLLAAhfzhfM7",
                "3PijZadO6sQXu4ZazsxjtGTyNfddeQ59jLAl1CCq");
    }
}
