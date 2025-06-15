package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.infowave.demo.fragments.ChatsFragment;
import com.infowave.demo.fragments.HomeFragment;
import com.infowave.demo.fragments.ProfileFragment;
import com.infowave.demo.fragments.SearchFragment;

public class Main extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Hamburger icon opens drawer
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Drawer toggle for hamburger sync
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle sidebar (drawer) item clicks
        navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.drawer_edit_profile) {
                Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.drawer_privacy) {
                Toast.makeText(this, "Privacy Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.drawer_blocked) {
                Toast.makeText(this, "Blocked Users Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.drawer_settings) {
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.drawer_logout) {
                Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.drawer_delete_account) {
                Toast.makeText(this, "Delete Account Clicked", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Bottom nav item clicks
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
