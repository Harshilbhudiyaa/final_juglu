package com.infowave.demo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.infowave.demo.R;
import com.infowave.demo.supabase.LocationRepository;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler;
    private Runnable locationTask;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler(Looper.getMainLooper());
        startForegroundService();
        startLocationUpdates();
    }

    private void startForegroundService() {
        String channelId = "location_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Juglu is tracking location")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1001, notification);
    }

    private void startLocationUpdates() {
        locationTask = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Send to Supabase
                        LocationRepository.updateLocation(getApplicationContext(), latitude, longitude);
                    }
                });

                handler.postDelayed(this, 30000); // Every 30 seconds
            }
        };

        handler.post(locationTask);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(locationTask);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
