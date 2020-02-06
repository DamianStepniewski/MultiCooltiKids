package pl.snikk.multicooltikids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileBroadcastReceiver extends BroadcastReceiver{

    private MyProfileActivity activity;

    public ProfileBroadcastReceiver(MyProfileActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Create a json object out of our response
        try {
            JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));

            // Process password change
            if (json.getString("action").equals("changePass")) {
                if (json.getBoolean("result"))
                    Toast.makeText(context, R.string.password_changed, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            // Process name change
            else if (json.getString("action").equals("changeName")) {
                if (json.getBoolean("result")) {
                    SharedPreferences data = context.getSharedPreferences("data", context.MODE_PRIVATE);
                    data.edit().putString("name", json.getString("name")).commit();
                } else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            // Process email change
            else if (json.getString("action").equals("changeEmail")) {
                if (json.getBoolean("result")) {
                    SharedPreferences data = context.getSharedPreferences("data", context.MODE_PRIVATE);
                    data.edit().putString("email", json.getString("email")).commit();
                } else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            // Process phone change
            else if (json.getString("action").equals("changePhone")) {
                if (json.getBoolean("result")) {
                    SharedPreferences data = context.getSharedPreferences("data", context.MODE_PRIVATE);
                    data.edit().putString("phone", json.getString("phone")).commit();
                } else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            // Add kid
            else if (json.getString("action").equals("addKid")) {
                if (json.getBoolean("result")) {
                    SharedPreferences sh = context.getSharedPreferences("data", context.MODE_PRIVATE);

                    ConnectionHandler ch = new ConnectionHandler(context);
                    ch.getKids(sh.getString("email", ""), sh.getString("ssid", ""));
                } else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            // Get kids
            else if (json.get("action").equals("getKids")) {
                if (json.getBoolean("result")) {
                    JSONArray kids = json.getJSONArray("kids");
                    activity.addKids(kids);
                } else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            // Remove kid
            else if (json.get("action").equals("removeKid")) {
                if (json.getBoolean("result")) {
                    SharedPreferences data = context.getSharedPreferences("data", context.MODE_PRIVATE);

                    ConnectionHandler ch = new ConnectionHandler(context);
                    ch.getKids(data.getString("email", ""), data.getString("ssid", ""));
                } else
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }

            activity.updateUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
