package pl.snikk.multicooltikids;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class ConversationActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private Context ctx;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        etMessage = (EditText) findViewById(R.id.editText_message);

        this.ctx = this;

        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));

        final SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);

        final ConnectionHandler ch = new ConnectionHandler(this);
        ch.getMessages(data.getString("email", ""), data.getString("ssid", ""), getIntent().getIntExtra("to_uid", 0));

        final LinearLayout container = (LinearLayout) findViewById(R.id.conversation_container);
        final ScrollView svConversation = (ScrollView) findViewById(R.id.scrollView_conversation);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("json")) {
                    try {
                        JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));

                        // Receive messages
                        if (json.getString("action").equals("getMessages")) {

                            if (json.has("messages")) {
                                container.removeAllViews();
                                JSONArray messages = json.getJSONArray("messages");
                                for (int i = 0; i < messages.length(); i++) {
                                    JSONObject message = messages.getJSONObject(i);

                                    int layoutId = -1;

                                    int toUid = message.getInt("to_uid");

                                    String timePrefix = "";

                                    // Check if our user is the sender
                                    if (toUid == getIntent().getIntExtra("to_uid", 0)) {
                                        layoutId = R.layout.message_to;
                                        timePrefix = getString(R.string.sent) + " ";
                                    } else {
                                        layoutId = R.layout.message_from;
                                        timePrefix = getString(R.string.received) + " ";
                                    }

                                    LayoutInflater inflater = LayoutInflater.from(ctx);
                                    RelativeLayout messageLayout = (RelativeLayout) inflater.inflate(layoutId, null, false);

                                    ((TextView) messageLayout.findViewById(R.id.textView_message_text)).setText(message.getString("text"));

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    long timestamp = Long.parseLong(message.getString("timestamp")) * 1000;
                                    Date meetingDate = new Date(timestamp);
                                    String dateString = dateFormat.format(meetingDate);

                                    ((TextView) messageLayout.findViewById(R.id.textView_message_time)).setText(timePrefix + dateString);

                                    container.addView(messageLayout);

                                    svConversation.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            svConversation.fullScroll(ScrollView.FOCUS_DOWN);
                                        }
                                    });
                                }
                            }
                        }

                        // Handle response from sending a message
                        else if (json.getString("action").equals("sendMessage")) {
                            ch.getMessages(data.getString("email", ""), data.getString("ssid", ""), getIntent().getIntExtra("to_uid", 0));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // Send the message on button click
        RelativeLayout buttonSend = (RelativeLayout) findViewById(R.id.button_message_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString();

                // Check if message is empty
                if (message.length() > 0) {
                    ch.sendMessage(data.getString("email", ""), data.getString("ssid", ""), message, getIntent().getIntExtra("to_uid", 0));
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
                } else
                    Toast.makeText(ctx, R.string.message_empty, Toast.LENGTH_SHORT).show();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter(Utils.REQUEST_MESSAGE));
    }

    public void refreshMessages() {
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        ConnectionHandler ch = new ConnectionHandler(this);
        ch.getMessages(data.getString("email", ""), data.getString("ssid", ""), getIntent().getIntExtra("to_uid", 0));
    }

    private void clearReferences(){
        Activity currActivity = Utils.getCurrentActivity();
        if (this.equals(currActivity))
            Utils.setCurrentActivity(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearReferences();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }
}
