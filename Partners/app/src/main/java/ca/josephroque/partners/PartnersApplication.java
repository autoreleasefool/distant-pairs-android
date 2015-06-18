package ca.josephroque.partners;

import android.app.Application;

import com.parse.Parse;

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

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "jWG1xELtom5AqG1jrv7swNBg3wgWlLLAAhfzhfM7",
                "3PijZadO6sQXu4ZazsxjtGTyNfddeQ59jLAl1CCq");
    }
}
