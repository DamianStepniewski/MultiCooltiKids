package pl.snikk.multicooltikids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class FriendsActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        ctx = this;

        getSupportActionBar().setTitle(R.string.friends);

        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);

        ConnectionHandler ch = new ConnectionHandler(this);
        ch.getFriends(data.getString("email", ""), data.getString("ssid", ""));

        final LinearLayout container = (LinearLayout) findViewById(R.id.friends_container);

        // Create broadcast receiver to parse data from friends request
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("json")) {
                    try {
                        JSONObject json = new JSONObject(Utils.rebuildString(intent.getStringExtra("json")));

                        // Check if user has friends
                        if (json.has("friends")) {
                            container.removeAllViews();

                            JSONArray friends = json.getJSONArray("friends");
                            for (int i=0; i<friends.length(); i++) {
                                JSONObject friend = friends.getJSONObject(i);
                                LayoutInflater inflater = LayoutInflater.from(ctx);
                                RelativeLayout friendLayout = (RelativeLayout) inflater.inflate(R.layout.friend, null, false);

                                ((TextView) friendLayout.findViewById(R.id.textView_friend_name)).setText(friend.getString("name"));
                                ((TextView) friendLayout.findViewById(R.id.textView_friend_phone)).setText(friend.getString("phone"));

                                // Check if this user has kids and add them to the layout
                                if (friend.has("kids")) {
                                    JSONArray kids = friend.getJSONArray("kids");
                                    LinearLayout kidsContainer = (LinearLayout) friendLayout.findViewById(R.id.layout_friend_kid_container);

                                    for (int j=0; j<kids.length(); j++) {
                                        JSONObject kid = kids.getJSONObject(j);

                                        LayoutInflater kidInflater = LayoutInflater.from(ctx);
                                        RelativeLayout kidView = (RelativeLayout) kidInflater.inflate(R.layout.layout_kid_found, null, false);

                                        ((TextView) kidView.findViewById(R.id.textView_kid_name)).setText(kid.getString("name"));

                                        int age = Utils.dateToAge(kid.getString("birthday"));
                                        String ageString = String.valueOf(age) + " " + (age == 1 ? getString(R.string.year_old) : getString(R.string.years_old));
                                        ((TextView) kidView.findViewById(R.id.textView_kid_age)).setText(ageString);

                                        String[] sex = getResources().getStringArray(R.array.sex_array);
                                        ((TextView) kidView.findViewById(R.id.textView_kid_sex)).setText(sex[kid.getInt("sex")]);

                                        ((TextView) kidView.findViewById(R.id.textView_kid_description)).setText(kid.getString("description"));

                                        // Dynamically load the avatar if it's set
                                        if (kid.getString("avatar").length() > 0) {
                                            NetworkImageView avatar = (NetworkImageView) kidView.findViewById(R.id.imageView_kid);

                                            RequestQueue mRequestQueue = Volley.newRequestQueue(ctx.getApplicationContext());

                                            ImageLoader mImageLoader = new ImageLoader(mRequestQueue,
                                                    new ImageLoader.ImageCache() {
                                                        private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

                                                        @Override
                                                        public Bitmap getBitmap(String url) {
                                                            return cache.get(url);
                                                        }

                                                        @Override
                                                        public void putBitmap(String url, Bitmap bitmap) {
                                                            cache.put(url, bitmap);
                                                        }
                                                    });
                                            avatar.setBackground(null);
                                            avatar.setImageUrl(Utils.URL + "avatars/" + kid.getString("avatar"), mImageLoader);
                                        }

                                        kidsContainer.addView(kidView);
                                    }
                                }

                                container.addView(friendLayout);
                            }
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
}
