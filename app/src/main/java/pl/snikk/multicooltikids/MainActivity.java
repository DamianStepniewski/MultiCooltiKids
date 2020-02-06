package pl.snikk.multicooltikids;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainBroadcastReceiver mReceiver;
    private SharedPreferences data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data = getSharedPreferences("data", MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        addButtonListeners();

        // Create broadcast receiver to parse data from json requests
        mReceiver = new MainBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter("jsonRequest"));

        // Check if the service is running and launch it if needed
        if (!isServiceRunning(UpdateCheckerService.class)) {
            Intent i = new Intent(this, UpdateCheckerService.class);
            this.startService(i);
        }
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
        updateNavDrawer(data.getBoolean("isLoggedIn", false));
        updateGUIcolors();
        Utils.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    private void clearReferences(){
        Activity currActivity = Utils.getCurrentActivity();
        if (this.equals(currActivity))
            Utils.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_profile) {

            // Load my profile activity
            Intent intent = new Intent(this, MyProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.hold);
        } else if (id == R.id.nav_status) {
            boolean status = data.getBoolean("status", false);
            toggleStatus(!status);
        } else if (id == R.id.nav_loginout) {
            if (!data.getBoolean("isLoggedIn", false)) {
                DialogFragment dialog = new LoginDialogFragment();
                dialog.show(getFragmentManager(), "LoginDialogFragment");
            } else
                doLogOut();
        }

        return true;
    }

    // Navigation drawer updating
    private void updateNavDrawer(boolean status) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (status) {
            // Enable drawer items
            navigationView.getMenu().findItem(R.id.nav_my_profile).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_status).setVisible(true);

            // Display username and email
            TextView textViewName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView_name);
            TextView textViewEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView_email);

            textViewName.setText(data.getString("name", ""));
            textViewEmail.setText(data.getString("email", ""));

            // Change login caption and icon
            navigationView.getMenu().findItem(R.id.nav_loginout).setTitle(R.string.log_out).setIcon(R.drawable.logout);
        } else {
            // Disable drawer items
            navigationView.getMenu().findItem(R.id.nav_my_profile).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_status).setVisible(false);;

            // Reset username and email
            TextView textViewName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView_name);
            TextView textViewEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView_email);

            textViewName.setText("");
            textViewEmail.setText("");

            // Change login caption and icon
            navigationView.getMenu().findItem(R.id.nav_loginout).setTitle(R.string.log_in).setIcon(R.drawable.login);
        }

        // Assign status icon
        if (data.getBoolean("status", false)) {
            navigationView.getMenu().getItem(1).setIcon(R.drawable.octagon);
        } else
            navigationView.getMenu().getItem(1).setIcon(R.drawable.octagon_outline);
    }

    // Update main GUI colors
    private void updateGUIcolors() {
        ArrayList<RelativeLayout> rlList = new ArrayList<RelativeLayout>();
        rlList.add((RelativeLayout) findViewById(R.id.button_messages));
        rlList.add((RelativeLayout) findViewById(R.id.button_friends));
        rlList.add((RelativeLayout) findViewById(R.id.button_venues));
        rlList.add((RelativeLayout) findViewById(R.id.button_next));
        rlList.add((RelativeLayout) findViewById(R.id.button_meetings));
        if (data.getBoolean("isLoggedIn", false)) {
            for (RelativeLayout button : rlList)
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            ((RelativeLayout) findViewById(R.id.button_newFriend)).setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        } else {
            for (RelativeLayout button : rlList)
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGray));
            ((RelativeLayout) findViewById(R.id.button_newFriend)).setBackgroundColor(ContextCompat.getColor(this, R.color.colorGray));
        }
        if (data.getBoolean("status", false))
            ((RelativeLayout)findViewById(R.id.button_newFriend)).setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        else
            ((RelativeLayout)findViewById(R.id.button_newFriend)).setBackgroundColor(ContextCompat.getColor(this, R.color.colorGray));
    }

    // Perform login
    public void doLogin() {
        toggleStatus(true);
        updateNavDrawer(data.getBoolean("isLoggedIn", false));
        updateGUIcolors();
    }

    // Perform logout
    public void doLogOut() {
        data.edit().putBoolean("isLoggedIn", false).commit();
        data.edit().putString("ssid", "").commit();
        toggleStatus(false);
        updateNavDrawer(data.getBoolean("isLoggedIn", false));
        updateGUIcolors();
    }

    // Toggle status
    private void toggleStatus(boolean status) {
        data.edit().putBoolean("status", status).commit();
        updateGUIcolors();
        updateNavDrawer(data.getBoolean("isLoggedIn", false));
    }

    // Add button listeners
    private void addButtonListeners() {
        MainMenuButtonListener buttonListener = new MainMenuButtonListener(this);

        RelativeLayout messagesButton = (RelativeLayout) findViewById(R.id.button_messages);
        messagesButton.setOnClickListener(buttonListener);

        RelativeLayout friendsButton = (RelativeLayout) findViewById(R.id.button_friends);
        friendsButton.setOnClickListener(buttonListener);

        RelativeLayout newFriendButton = (RelativeLayout) findViewById(R.id.button_newFriend);
        newFriendButton.setOnClickListener(buttonListener);

        RelativeLayout venuesButton = (RelativeLayout) findViewById(R.id.button_venues);
        venuesButton.setOnClickListener(buttonListener);

        RelativeLayout nextPlaceButton = (RelativeLayout) findViewById(R.id.button_next);
        nextPlaceButton.setOnClickListener(buttonListener);

        RelativeLayout myMeetingsButton = (RelativeLayout) findViewById(R.id.button_meetings);
        myMeetingsButton.setOnClickListener(buttonListener);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
