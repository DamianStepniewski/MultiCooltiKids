package pl.snikk.multicooltikids;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UpdateCheckerService extends Service implements LocationListener {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture task;

    private final static int INTERVAL_SCREEN_OFF = 60;
    private final static int INTERVAL_SCREEN_ON = 10;

    private ServiceBroadcastReceiver mReceiver;
    private ScreenReceiver screenReceiver;

    private LocationManager locationManager;
    private String provider;

    private boolean screenOn = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        task = scheduler.scheduleAtFixedRate(loginCheck, 10, INTERVAL_SCREEN_ON, TimeUnit.SECONDS);

        // Create a broadcast receiver for screen on/off events
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        screenReceiver = new ScreenReceiver();
        registerReceiver(screenReceiver, filter);

        // Create broadcast receiver to parse data from json requests
        mReceiver = new ServiceBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter("jsonRequest"));

        // Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        // Request location checks
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }

        return Service.START_NOT_STICKY;
    }

    final Runnable loginCheck = new Runnable() {
        public void run() {
            // Send geolocation data to the server
            SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
            float lng = data.getFloat("long", 0);
            float lat = data.getFloat("lat", 0);

            ConnectionHandler ch = new ConnectionHandler(getApplication());
            ch.setPosition(data.getString("email", ""), data.getString("ssid", ""), String.valueOf(lat), String.valueOf(lng));

            // Check for updates
            String ssid = data.getString("ssid", "");
            if (ssid.length() > 0) {
                Log.i("MCKService", "Checking for updates.");
                ch = new ConnectionHandler(getApplicationContext());
                ch.checkForUpdates(data.getString("email", ""), ssid);
            } else {
                data.edit().putBoolean("isLoggedIn", false).commit();
                data.edit().putString("ssid", "").commit();

                // Check if the current activity is MainActivity
                if (Utils.getCurrentActivity() instanceof MainActivity) {

                    // Perform UI logout
                    ((MainActivity) Utils.getCurrentActivity()).doLogOut();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());

        Log.i("MCKService", "Location changed to: "+String.valueOf(lat)+"; "+String.valueOf(lng));

        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        data.edit().putFloat("lat", (float) lat).commit();
        data.edit().putFloat("long", (float) lng).commit();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                task.cancel(false);
                task = scheduler.scheduleAtFixedRate(loginCheck, 10, INTERVAL_SCREEN_OFF, TimeUnit.SECONDS);
                Log.i("MCKService", "Screen turned off, updates are now checked every "+String.valueOf(INTERVAL_SCREEN_OFF)+" seconds.");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                task.cancel(false);
                task = scheduler.scheduleAtFixedRate(loginCheck, 10, INTERVAL_SCREEN_ON, TimeUnit.SECONDS);
                Log.i("MCKService", "Screen turned on, updates are now checked every "+String.valueOf(INTERVAL_SCREEN_ON)+" seconds.");
            }
        }

    }
}
