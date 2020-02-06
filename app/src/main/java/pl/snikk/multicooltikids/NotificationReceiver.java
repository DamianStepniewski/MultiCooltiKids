package pl.snikk.multicooltikids;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        int meetingId = intent.getIntExtra("com.multicoolti.meetingId", 0);

        SharedPreferences data = context.getSharedPreferences("data", context.MODE_PRIVATE);
        String email = data.getString("email", "");
        String ssid = data.getString("ssid", "");

        ConnectionHandler ch = new ConnectionHandler(context);
        if (intent.getAction().equals("com.multicoolti.accept")) {
            ch.acceptMeeting(meetingId, ssid, email);
        }
        else if (intent.getAction().equals("com.multicoolti.decline")) {
            ch.declineMeeting(meetingId, ssid, email);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(meetingId);
    }
}
