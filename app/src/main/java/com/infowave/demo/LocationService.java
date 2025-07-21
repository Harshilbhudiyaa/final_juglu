package com.infowave.demo;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.infowave.demo.supabase.LocationRepository;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.d("LocationService", "Service created and starting...");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForegroundService();
        startLocationUpdates();
    }

    private void startForegroundService() {
        android.util.Log.d("LocationService", "Starting foreground service...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Juglu is tracking location")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void startLocationUpdates() {
        android.util.Log.d("LocationService", "Attempting to start location updates...");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("LocationService", "Permission not granted. Stopping service.");
            stopSelf();
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(30000)
                .setFastestInterval(25000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                android.util.Log.d("LocationService", "onLocationResult triggered.");
                if (result == null) return;
                Location location = result.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    android.util.Log.d("LocationService", "Got location: " + latitude + ", " + longitude);
                    LocationRepository.updateLocation(getApplicationContext(), latitude, longitude);
                } else {
                    android.util.Log.e("LocationService", "Location object is null!");
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("LocationService", "Permission revoked while running, stopping.");
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}