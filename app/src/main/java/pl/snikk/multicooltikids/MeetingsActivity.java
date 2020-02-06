package pl.snikk.multicooltikids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MeetingsActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        getSupportActionBar().setTitle(R.string.my_meetings);

        ctx = this;

        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        ConnectionHandler ch = new ConnectionHandler(this);
        ch.getMeetings(data.getString("email", ""), data.getString("ssid", ""));

        final LinearLayout upcomingContainer = (LinearLayout) findViewById(R.id.upcoming_meetings_container);
        final LinearLayout pastContainer = (LinearLayout) findViewById(R.id.past_meetings_container);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("json")) {
                    try {
                        JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));
                        if (json.getBoolean("result")) {
                            boolean hasViews = false;

                            JSONArray meetings = json.getJSONArray("meetings");

                            for (int i=0; i<meetings.length(); i++) {
                                JSONObject meeting = (JSONObject) meetings.get(i);

                                LayoutInflater inflater = LayoutInflater.from(ctx);
                                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.meeting, null, false);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                long timestamp = Long.parseLong(meeting.getString("time"))*1000;
                                Date meetingDate = new Date(timestamp);
                                String dateString = dateFormat.format(meetingDate);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                String timeString = timeFormat.format(meetingDate);

                                Date now = new Date();

                                ((TextView)layout.findViewById(R.id.textView_meeting_from)).setText(meeting.getString("from_name"));
                                ((TextView)layout.findViewById(R.id.textView_meeting_date)).setText(dateString);
                                ((TextView)layout.findViewById(R.id.textView_meeting_time)).setText(timeString);
                                ((TextView)layout.findViewById(R.id.textView_meeting_address)).setText(meeting.getString("address"));

                                if (!hasViews)
                                    upcomingContainer.removeAllViews();
                                hasViews = true;

                                if (now.before(meetingDate)) {
                                    upcomingContainer.addView(layout);
                                    ((TextView)layout.findViewById(R.id.textView_meeting_status)).setText(getString(R.string.upcoming_meetings));
                                }
                                else  {
                                    pastContainer.addView(layout);
                                    ((TextView)layout.findViewById(R.id.textView_meeting_status)).setText(getString(R.string.past_meetings));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter(Utils.REQUEST_MEETINGS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }
}
