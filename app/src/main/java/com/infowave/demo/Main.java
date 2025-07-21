package com.infowave.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.infowave.demo.fragments.*;

public class Main extends AppCompatActivity {

    private NavigationView navView;
    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 111;
    private static final int REQUEST_CHECK_SETTINGS = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.util.Log.d("MainActivity", "onCreate: Main activity started");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);  // <-- यह लाइन मेनू और क्लिक के लिए जरूरी है

        checkAndRequestLocationPermission();

        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left, top, right, bottom);
                return insets.consumeSystemWindowInsets();
            }
        });

        navView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_chats) {
                loadFragment(new ChatsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkAndRequestLocationPermission() {
        android.util.Log.d("MainActivity", "checkAndRequestLocationPermission called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            checkLocationEnabledAndStartService();
        }
    }

    private void checkLocationEnabledAndStartService() {
        android.util.Log.d("MainActivity", "checkLocationEnabledAndStartService called");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                android.util.Log.d("MainActivity", "Location settings satisfied, starting service");
                startLocationService();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                android.util.Log.e("MainActivity", "Location settings not satisfied");
                if (e instanceof ResolvableApiException) {
                    try {
                        ((ResolvableApiException) e).startResolutionForResult(Main.this, REQUEST_CHECK_SETTINGS);
                    } catch (Exception sendEx) {
                        Toast.makeText(Main.this, "Could not show location dialog. Please enable GPS in settings.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                } else {
                    Toast.makeText(Main.this, "Location settings are inadequate. Please enable GPS.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d("MainActivity", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                android.util.Log.d("MainActivity", "GPS enabled, starting location service");
                startLocationService();
            } else {
                android.util.Log.d("MainActivity", "User cancelled GPS dialog");
                Toast.makeText(this, "Location must be enabled for live tracking.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        android.util.Log.d("MainActivity", "onRequestPermissionsResult called");
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("MainActivity", "Location permission granted");
                checkLocationEnabledAndStartService();
            } else {
                android.util.Log.d("MainActivity", "Location permission denied");
                Toast.makeText(this, "Location permission is required to track your location.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationService() {
        android.util.Log.d("MainActivity", "startLocationService() called.");
        Intent intent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        android.util.Log.d("MainActivity", "Menu item clicked with id: " + id);

        try {
            if (id == R.id.action_notifications) {
                android.util.Log.d("MainActivity", "Opening NewPostActivity");
                Intent intent = new Intent(this, NotificationActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.location) {
                android.util.Log.d("MainActivity", "Opening Map Activity");
                Intent intent = new Intent(this, Map.class);
                startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error opening activity", e);
            Toast.makeText(this, "Error opening activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
