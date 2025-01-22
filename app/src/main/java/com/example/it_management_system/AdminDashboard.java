package com.example.it_management_system;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboard extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private View fragmentContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigationAdmin);
        fragmentManager = getSupportFragmentManager();
        fragmentContainer = findViewById(R.id.fragment_containerAdmin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean isKeyboardVisible = imeInsets.bottom > 0;


            bottomNavigationView.setVisibility(isKeyboardVisible ? View.GONE : View.VISIBLE);
            return insets;
        });

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_containerAdmin, new HomeAdminFragment());
            fragmentTransaction.commit();
        }


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_homeAdmin) {
                selectedFragment = new HomeAdminFragment();
            } else if (itemId == R.id.nav_ticketsAdmin) {
                selectedFragment = new TicketAdminFragment();
            } else if (itemId == R.id.nav_executives) {
                selectedFragment = new ExecutivesListAdminFragment();
            } else {
                selectedFragment = new SettingAdminFragment();
            }

            if (selectedFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_containerAdmin, selectedFragment);
                transaction.commit();
            }
            return true;
        });

    }
}