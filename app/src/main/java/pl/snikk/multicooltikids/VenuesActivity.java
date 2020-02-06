package pl.snikk.multicooltikids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class VenuesActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private ArrayList<Venue> venues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        getSupportActionBar().setTitle(R.string.venues);

        venues = new ArrayList<Venue>();

        ConnectionHandler ch = new ConnectionHandler(this);

        ch.getVenues();

        // Create broadcast receiver to parse data from venues request
        mReceiver = new BroadcastReceiver() {
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
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter(Utils.REQUEST_VENUES));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }
}
