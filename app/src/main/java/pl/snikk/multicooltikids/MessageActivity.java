package pl.snikk.multicooltikids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        getSupportActionBar().setTitle(R.string.messages);

        ctx = this;

        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);

        ConnectionHandler ch = new ConnectionHandler(this);
        ch.getFriends(data.getString("email", ""), data.getString("ssid", ""));

        final ListView list = (ListView) findViewById(R.id.listView_messages);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int uid = (int) view.getTag();

                String name = ((TextView) view.findViewById(R.id.textView_messages_friend_name)).getText().toString();

                Intent intent = new Intent(ctx, ConversationActivity.class);
                intent.putExtra("to_uid", uid);
                intent.putExtra("name", name);
                ctx.startActivity(intent);
            }
        });

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("json")) {
                    try {
                        JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));

                        // Check if user has friends
                        if (json.has("friends")) {
                            HashMap<String, Integer> data = new HashMap<>();
                            ArrayList<String> array = new ArrayList<>();

                            JSONArray friends = json.getJSONArray("friends");
                            for (int i=0; i<friends.length(); i++) {
                                JSONObject friend = friends.getJSONObject(i);
                                data.put(friend.getString("name"), friend.getInt("uid"));
                                array.add(friend.getString("name"));
                            }

                            ArrayAdapter<String> adapter = new ListAdapter(context, data, array);

                            list.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter(Utils.REQUEST_FRIENDS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private class ListAdapter extends ArrayAdapter<String> {

        private Context context;
        private HashMap<String, Integer> data;
        private ArrayList<String> array;

        public ListAdapter(Context context, HashMap<String, Integer> objects, ArrayList<String> array) {
            super(context, -1, array);
            this.context = context;
            this.data = objects;
            this.array = array;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.message_row_group, parent, false);

            TextView tv = (TextView) rowView.findViewById(R.id.textView_messages_friend_name);
            String name = array.get(position);
            tv.setText(name);
            rowView.setTag(data.get(name));

            return rowView;
        }
    }
}
