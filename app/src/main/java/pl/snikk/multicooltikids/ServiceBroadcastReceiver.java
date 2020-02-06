package pl.snikk.multicooltikids;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // Create a json object out of our response
            JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));

            SharedPreferences data = context.getSharedPreferences("data", context.MODE_PRIVATE);

            // Check if everything's ok
            if (json.getString("status").equals("ok")) {
                String action = json.getString("action");

                // Get the result of updates call
                if (action.equals("update")) {
                    if(json.getBoolean("isLoggedIn")) {

                        // Check for new messages
                        if (json.getBoolean("messages")) {

                            // Check if user is currently looking at the conversation
                            if (Utils.getCurrentActivity() instanceof ConversationActivity) {
                                ((ConversationActivity) Utils.getCurrentActivity()).refreshMessages();
                            } else {
                                // Create notification
                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                                        .setContentTitle(context.getString(R.string.new_message))
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentText("");

                                Intent resultIntent = new Intent(context, MessageActivity.class);

                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

                                // Adds the back stack for the Intent (but not the Intent itself)
                                stackBuilder.addParentStack(MessageActivity.class);

                                // Adds the Intent that starts the Activity to the top of the stack
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                notificationBuilder.setContentIntent(resultPendingIntent);

                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                Notification nt = notificationBuilder.build();
                                nt.flags |= Notification.FLAG_AUTO_CANCEL;
                                notificationManager.notify(-1, nt);
                            }
                        }

                        // Check for new meetings
                        if (json.has("meetings")) {

                            // Create notifications for each meeting
                            JSONArray meetings = json.getJSONArray("meetings");
                            for (int i=0; i < meetings.length(); i++) {
                                JSONObject meeting = meetings.getJSONObject(i);

                                // Create accept intent
                                Intent acceptIntent = new Intent(context, NotificationReceiver.class);
                                acceptIntent.setAction("com.multicoolti.accept");
                                acceptIntent.putExtra("com.multicoolti.meetingId", meeting.getInt("id"));
                                PendingIntent pendingAcceptIntent = PendingIntent.getBroadcast(context, 0, acceptIntent, PendingIntent.FLAG_ONE_SHOT);

                                // Create decline intent
                                Intent declineIntent = new Intent(context, NotificationReceiver.class);
                                declineIntent.setAction("com.multicoolti.decline");
                                declineIntent.putExtra("com.multicoolti.meetingId", meeting.getInt("id"));
                                PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(context, 0, declineIntent, PendingIntent.FLAG_ONE_SHOT);

                                // Parse date and time of the meeting
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                long timestamp = Long.parseLong(meeting.getString("time"))*1000;
                                Date meetingDate = new Date(timestamp);
                                String dateString = dateFormat.format(meetingDate);

                                // Create notification
                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                                        .setContentTitle(context.getString(R.string.new_meeting) + " " + meeting.getString("from"))
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentText(meeting.getString("name"))
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                                .bigText(meeting.getString("name") + "\n" + context.getString(R.string.address) + " " + meeting.getString("address") + "\n" + context.getString(R.string.date_and_time) + " " + dateString)
                                                .setBigContentTitle(context.getString(R.string.new_meeting) + " " + meeting.get("from")))
                                        .addAction(R.drawable.ic_check_white_24dp, context.getString(R.string.accept), pendingAcceptIntent)
                                        .addAction(R.drawable.ic_close_white_24dp, context.getString(R.string.decline), pendingDeclineIntent);

                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(meeting.getInt("id"), notificationBuilder.build());
                            }
                        }
                    } else {
                        // SSIDs don't match, log the user out
                        Log.i("MCKService", "SSID mismatch, user logged out.");
                        data.edit().putBoolean("isLoggedIn", false).commit();
                        data.edit().putString("ssid", "").commit();
                        Toast.makeText(context, R.string.logged_out, Toast.LENGTH_LONG).show();

                        // Check if the current activity is MainActivity
                        if (Utils.getCurrentActivity() instanceof MainActivity) {

                            // Perform UI logout
                            ((MainActivity) Utils.getCurrentActivity()).doLogOut();
                        }
                    }
                    Log.i("MCKService", "Updates check finished.");
                }
            }
        } catch (JSONException e) {
            Log.i("MCKService", "Updates check finished.");
        }
    }
}
