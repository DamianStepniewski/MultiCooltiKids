package pl.snikk.multicooltikids;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainBroadcastReceiver extends BroadcastReceiver {

    private MainActivity activity;
    private SharedPreferences data;

    public MainBroadcastReceiver(MainActivity activity) {
        this.activity = activity;
        this.data = activity.getSharedPreferences("data", activity.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // Create a json object out of our response
            JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));

            // Check if everything's ok
            if (json.getString("status").equals("ok")) {
                String action = json.getString("action");

                // Process registration/log in
                if (action.equals("register") || action.equals("login")) {

                    // Action was successful
                    if (json.getBoolean("result")) {

                        // Save name, email and ssid into preferences
                        data.edit().putString("name",json.getString("name")).commit();
                        data.edit().putString("email", json.getString("email")).commit();
                        data.edit().putString("ssid",json.getString("ssid")).commit();
                        data.edit().putString("phone", json.getString("phone")).commit();
                        data.edit().putBoolean("isLoggedIn", true).commit();

                        // Perform login on the UI
                        activity.doLogin();
                    } else {

                        // Registration failed
                        if (action.equals("register"))
                            Toast.makeText(context, R.string.email_used, Toast.LENGTH_SHORT).show();

                        // Log in failed
                        if (action.equals("login"))
                            Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                }

                // Process finding new friend
                else if (action.equals("findFriend")) {

                    if (json.getString("result2").equals("not_found"))
                        Toast.makeText(context, R.string.friend_not_found, Toast.LENGTH_LONG).show();
                    else if (json.getString("result2").equals("limit_exceeded"))
                        Toast.makeText(context, R.string.friend_limit_exceeded, Toast.LENGTH_LONG).show();
                    else {
                        JSONObject user = json.getJSONObject("user");
                        Bundle args = new Bundle();

                        if (user.has("kids")) {
                            JSONArray kids = user.getJSONArray("kids");

                            ArrayList<JSONObject> kidsArray = new ArrayList<>();

                            for (int i = 0; i < kids.length(); i++) {
                                JSONObject kid = (JSONObject) kids.get(i);
                                kidsArray.add(kid);
                            }

                            args.putSerializable("kids", kidsArray);
                        }

                        FindFriendDialogFragment findFriendFragment = new FindFriendDialogFragment();
                        args.putString("name", user.getString("name"));
                        args.putString("phone", user.getString("phone"));
                        args.putString("email", user.getString("email"));
                        findFriendFragment.setArguments(args);
                        findFriendFragment.show(activity.getFragmentManager(), "FindFrienddDialogFragment");
                    }

                // Process finding nearest venue
                } else if (action.equals("findVenue")) {
                    if (json.getBoolean("result")) {
                        Bundle args = new Bundle();

                        args.putString("name", json.getJSONObject("venue").getString("name"));
                        args.putString("address", json.getJSONObject("venue").getString("address"));
                        args.putString("time", json.getJSONObject("venue").getString("time"));
                        args.putString("description", json.getJSONObject("venue").getString("description"));

                        NextVenueDialogFragment nextPlaceDialog = new NextVenueDialogFragment();
                        nextPlaceDialog.setArguments(args);
                        nextPlaceDialog.show(activity.getFragmentManager(),"NextVenueDialogFragment");
                    }

                    // Process getting user meetings
                } else if (action.equals("getMeetings")) {
                    if (json.getBoolean("result")) {

                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
};
