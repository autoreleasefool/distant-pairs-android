package ca.josephroque.partners.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ca.josephroque.partners.util.MessageUtil;

/**
 * Created by Joseph Roque on 2015-06-30.
 *
 * INSERT CLASS DESCRIPTION HERE
 */
public class MessageReceiver extends BroadcastReceiver
{

    private static final String TAG = "MessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String jsonData = intent.getStringExtra("com.parse.Data");
        Log.i(TAG, "Message received:" + jsonData);
        try
        {
            JSONObject data = new JSONObject(jsonData);
            String message = data.getString("message");
            String timestamp = data.getString("timestamp");
            String id = data.getString("id");

            Intent messageIntent = new Intent(MessageUtil.ACTION_MESSAGE_RECEIVED);
            messageIntent.putExtra("message", message);
            messageIntent.putExtra("timestamp", timestamp);
            messageIntent.putExtra("id", id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
        }
        catch (JSONException ex)
        {
            Log.e(TAG, "JSON error: ", ex);
            // do nothing - app may not be open
        }
    }
}
