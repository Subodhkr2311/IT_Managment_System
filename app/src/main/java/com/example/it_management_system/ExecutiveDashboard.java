package com.example.it_management_system;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ExecutiveDashboard extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_executive_dashboard);

        setupEdgeToEdge();


        MaterialToolbar toolbar = findViewById(R.id.toolbarExecutive);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigationExecutive);
        fragmentManager = getSupportFragmentManager();
        View fragmentContainer = findViewById(R.id.fragment_containerExecutive);

        if (savedInstanceState == null) {
            loadFragment(new HomeExecutiveFragment());
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            int imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime()) ? View.GONE : View.VISIBLE;
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            boolean isKeyboardVisible = imeInsets.bottom > 0;

            bottomNavigationView.setVisibility(imeVisible);
            int bottomPadding = isKeyboardVisible ? imeInsets.bottom : systemInsets.bottom;
            fragmentContainer.setPadding(0, 0, 0, bottomPadding);
            return insets;
        });
        // Set Bottom Navigation item select listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_homeExecutive) {
                selectedFragment = new HomeExecutiveFragment();
            } else if (itemId == R.id.nav_MyticketsExecutive) {
                selectedFragment = new MyticketsExecutiveFragment();
            } else if (itemId == R.id.settingsExecutive) {
                selectedFragment = new SettingExecutiveFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void setupEdgeToEdge() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            v.setPadding(0, windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        // Set the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_containerExecutive, fragment);
        transaction.commit();
    }
}