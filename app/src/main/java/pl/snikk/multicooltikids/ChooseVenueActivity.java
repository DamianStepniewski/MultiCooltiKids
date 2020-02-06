package pl.snikk.multicooltikids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChooseVenueActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private String email;
    private ArrayList<Venue> venues;
    private RadioButton radioSelectVenue, radioOfferVenue;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_venue);

        ctx = this;

        // Create and register broadcast receiver handling json data
        mReceiver = new VenueBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter(Utils.REQUEST_VENUES));

        // Get datepicker, disable calendar view, set min date as current date
        datePicker = (DatePicker) findViewById(R.id.datePicker_meeting);
        datePicker.setCalendarViewShown(false);
        datePicker.setMinDate(new Date().getTime() - 1000);

        // Get timepicker, set 24h view
        timePicker = (TimePicker) findViewById(R.id.timePicker_meeting);
        timePicker.setIs24HourView(true);

        // Set Offer Venue radio button checked as default
        radioOfferVenue = (RadioButton) findViewById(R.id.radioButton_new_venue);
        radioOfferVenue.setChecked(true);

        radioSelectVenue = (RadioButton) findViewById(R.id.radioButton_select_venue);

        email = getIntent().getStringExtra("email");
        venues = new ArrayList<Venue>();
        ConnectionHandler ch = new ConnectionHandler(this);
        ch.getVenues();

        // Set accept button action listener
        RelativeLayout buttonAccept = (RelativeLayout) findViewById(R.id.button_venue_accept);
        buttonAccept.setOnClickListener(new AcceptClickListener());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    // Accept button click listener
    private class AcceptClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
            String fromEmail = data.getString("email", "");
            String ssid = data.getString("ssid", "");

            ConnectionHandler ch = new ConnectionHandler(ctx);

            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            long timestamp = calendar.getTimeInMillis()/1000;

            // If user is offering a venue
            if (radioOfferVenue.isChecked()) {
                String name = ((EditText)findViewById(R.id.editText_venue_name)).getText().toString();
                String address = ((EditText)findViewById(R.id.editText_venue_address)).getText().toString();

                if (name.length() > 0 && address.length() > 0) {
                    ch.addMeeting(fromEmail, email, timestamp, name, address, ssid);
                    Toast.makeText(ctx, R.string.invitation_sent, Toast.LENGTH_LONG).show();
                    finish();
                } else
                    Toast.makeText(ctx, R.string.name_address_invalid, Toast.LENGTH_LONG).show();
            }

            // If user is selecting a venue
            else if (radioSelectVenue.isChecked()) {
                String name = radioSelectVenue.getText().toString();
                if (!name.equals(getString(R.string.select_venue))) {
                    String address = radioSelectVenue.getTag().toString();
                    ch.addMeeting(fromEmail, email, timestamp, name, address, ssid);
                    Toast.makeText(ctx, R.string.invitation_sent, Toast.LENGTH_LONG).show();
                    finish();
                } else
                    Toast.makeText(ctx, R.string.error_select_venue, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Listener for click events on venues
    private class ChildClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            radioSelectVenue.setChecked(true);
            TextView name = (TextView)v.findViewById(R.id.venues_name);
            TextView address = (TextView)v.findViewById(R.id.venues_address);
            radioSelectVenue.setText(name.getText());
            radioSelectVenue.setTag(address.getText().toString());
            return false;
        }
    }

    // Broadcast receiver handling json data
    private class VenueBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("json")) {
                try {
                    JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));
                    JSONArray array = json.getJSONObject("venues").getJSONArray("monuments");
                    ArrayList<Event> events = new ArrayList<>();

                    for (int i=0; i<array.length(); i++) {
                        JSONObject monument = (JSONObject) array.get(i);
                        Event event = new Event();

                        event.name = monument.getString("name");
                        event.address = monument.getString("address");
                        event.description = monument.getString("description");

                        events.add(event);
                    }

                    Venue venue = new Venue(events, getString(R.string.monuments));

                    venues.add(venue);

                    array = json.getJSONObject("venues").getJSONArray("events");
                    events = new ArrayList<>();

                    for (int i=0; i<array.length(); i++) {
                        JSONObject ev = (JSONObject) array.get(i);
                        Event event = new Event();

                        event.name = ev.getString("name");
                        event.address = ev.getString("address");
                        event.description = ev.getString("description");
                        event.time = ev.getString("time");

                        events.add(event);
                    }

                    venue = new Venue(events, getString(R.string.events));

                    venues.add(venue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ExpandableListView listView = (ExpandableListView) findViewById(R.id.listViewVenues);

            VenuesAdapter adapter = new VenuesAdapter(context, venues);

            listView.setAdapter(adapter);
            listView.setGroupIndicator(null);
            listView.setOnChildClickListener(new ChildClickListener());
        }
    }
}
