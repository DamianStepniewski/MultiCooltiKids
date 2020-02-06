package pl.snikk.multicooltikids;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class MyProfileActivity extends AppCompatActivity {

    private Context ctx = this;

    private AddKidDialogFragment addKidDialogFragment;
    private ProfileBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_my_profile);

        getSupportActionBar().setTitle(R.string.my_profile);

        // Add button listeners
        RelativeLayout rlPassword = (RelativeLayout) findViewById(R.id.button_settings_changePassword);
        rlPassword.setOnClickListener(new PasswordChangeClickListener());

        RelativeLayout rlAddKid = (RelativeLayout) findViewById(R.id.button_settings_addKid);
        rlAddKid.setOnClickListener(new AddKidClickListener());

        // Create broadcast receiver to parse data from json requests
        mReceiver = new ProfileBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter("jsonRequest"));

        updateUI();

        EditText etName = (EditText) findViewById(R.id.editText_settings_name);
        EditText etEmail = (EditText) findViewById(R.id.editText_settings_email);
        EditText etPhone = (EditText) findViewById(R.id.editText_settings_telephone);

        ETOnChangeListener etAC = new ETOnChangeListener();
        etName.setOnFocusChangeListener(etAC);
        etName.setOnEditorActionListener(etAC);
        etEmail.setOnFocusChangeListener(etAC);
        etEmail.setOnEditorActionListener(etAC);
        etPhone.setOnFocusChangeListener(etAC);
        etPhone.setOnEditorActionListener(etAC);

        SharedPreferences sh = getSharedPreferences("data", MODE_PRIVATE);

        ConnectionHandler ch = new ConnectionHandler(this);
        ch.getKids(sh.getString("email", ""), sh.getString("ssid", ""));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_to_left);
    }

    public void updateUI() {
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);

        EditText etName = (EditText) findViewById(R.id.editText_settings_name);
        EditText etEmail = (EditText) findViewById(R.id.editText_settings_email);
        EditText etPhone = (EditText) findViewById(R.id.editText_settings_telephone);

        etName.setText(data.getString("name", ""));
        etEmail.setText(data.getString("email", ""));
        etPhone.setText(data.getString("phone", ""));
    }

    public void addKids(JSONArray kids) {
        LinearLayout container = (LinearLayout)findViewById(R.id.layout_kid_container);
        container.removeAllViews();
        try {
            for (int i=0; i<kids.length(); i++) {
                JSONObject kid = (JSONObject) kids.get(i);
                LayoutInflater inflater = LayoutInflater.from(this);
                RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.layout_kid, null, false);

                // Handle kid deleting
                ImageView buttonDelete = (ImageView) layout.findViewById(R.id.button_kid_delete);
                buttonDelete.setTag(kid.getInt("kid"));
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);

                        ConnectionHandler ch = new ConnectionHandler(ctx);
                        ch.removeKid(data.getString("email", ""), data.getString("ssid", ""), v.getTag().toString());
                    }
                });

                int age = Utils.dateToAge(kid.getString("birthday"));
                String ageString = String.valueOf(age) + " " + (age == 1 ? getString(R.string.year_old) : getString(R.string.years_old));

                String[] sex = getResources().getStringArray(R.array.sex_array);

                ((TextView) layout.findViewById(R.id.textView_kid_name)).setText(kid.getString("name"));
                ((TextView) layout.findViewById(R.id.textView_kid_age)).setText(ageString);
                ((TextView) layout.findViewById(R.id.textView_kid_sex)).setText(sex[kid.getInt("sex")]);
                ((TextView) layout.findViewById(R.id.textView_kid_description)).setText(kid.getString("description"));

                if (kid.getString("avatar").length() > 0) {
                    NetworkImageView avatar = (NetworkImageView) layout.findViewById(R.id.imageView_kid);

                    RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());

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

                container.addView(layout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Listener for click event on "Change Password" button
    private class PasswordChangeClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            DialogFragment dialog = new PasswordChangeDialogFragment();
            dialog.show(getFragmentManager(),"PasswordChangeDialogFragment");
        }
    }

    // Listener for click event on "Add a Kid" button and kid details modification
    private class AddKidClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            addKidDialogFragment = new AddKidDialogFragment();
            addKidDialogFragment.show(getFragmentManager(),"AddKidDialogFragment");
        }
    }

    private void updateData(TextView v) {
        ConnectionHandler ch = new ConnectionHandler(this);
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        if (v.equals(findViewById(R.id.editText_settings_name)))
            ch.changeName(v.getText().toString(), data.getString("email", ""), data.getString("ssid", ""));
        else if (v.equals(findViewById(R.id.editText_settings_email))) {
            String email = v.getText().toString();
            if (email.matches(Utils.EMAIL_REGEX))
                ch.changeEmail(v.getText().toString(), data.getString("email", ""), data.getString("ssid", ""));
            else
                Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
        }
        else if (v.equals(findViewById(R.id.editText_settings_telephone)))
            ch.changePhone(v.getText().toString(), data.getString("email", ""), data.getString("ssid", ""));
    }

    // Listener for changes in edit texts associated with Name, Email and Phone
    private class ETOnChangeListener implements View.OnFocusChangeListener, TextView.OnEditorActionListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus)
                updateData((TextView)v);
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateData(v);
            }
            return false;
        }


    }
}
