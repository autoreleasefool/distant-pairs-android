package ca.josephroque.partners.message;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ca.josephroque.partners.util.MessageUtils;

/**
 * Created by Joseph Roque on 2015-06-30. Handles push notifications from GCM.
 */
public class MessageReceiver
        extends BroadcastReceiver
{

    /** To identify output from this class in the Logcat. */
    @SuppressWarnings("unused")
    private static final String TAG = "MessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String jsonData = intent.getStringExtra("com.parse.Data");
        try
        {
            JSONObject data = new JSONObject(jsonData);
            String message = data.getString("message");
            String timestamp = data.getString("timestamp");
            String id = data.getString("id");

            Intent messageIntent = new Intent(MessageUtils.ACTION_MESSAGE_RECEIVED);
            messageIntent.putExtra("message", message);
            messageIntent.putExtra("timestamp", timestamp);
            messageIntent.putExtra("id", id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);

            if (!(MessageUtils.LOGIN_MESSAGE.equals(message)
                    || MessageUtils.LOGOUT_MESSAGE.equals(message)
                    || MessageUtils.VISITED_MESSAGE.equals(message)))
            {
                ParseObject thought = ParseObject.createWithoutData("Thought", id);
                thought.fetchInBackground(new GetCallback<ParseObject>()
                {
                    @Override
                    public void done(ParseObject parseObject, ParseException e)
                    {
                        if (e == null && parseObject != null)
                        {
                            parseObject.put("timeRead", new Date().getTime());
                            parseObject.saveInBackground();
                        }
                    }
                });
            }
        }
        catch (JSONException ex)
        {
            // do nothing - app may not be open
        }
    }
}
