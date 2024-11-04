package com.example.it_management_system;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class UserDashboard extends AppCompatActivity {

    private TextView notificationCount;
    private ImageView notificationIcon;
    private int unreadNotificationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard); // Make sure to name your layout file correctly

        MaterialToolbar toolbar = findViewById(R.id.toolbarUser);
        notificationIcon = findViewById(R.id.notification_iconUser);
        notificationCount = findViewById(R.id.notification_countUser);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigationUser);

        // Set up the toolbar
        setSupportActionBar(toolbar);
        // Remove the default title (app name) from the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboard.this, NotificationActivityUser.class);
                startActivity(intent);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_homeUser) {
                    selectedFragment = new RaiseComplaintFragment(); // Replace with your HomeFragment
                } else if (itemId == R.id.nav_ticketsUser) {
                    selectedFragment = new TicketsHistoryUsersFragment(); // Replace with your TicketsFragment
                } else if (itemId == R.id.nav_profileUser) {
                    selectedFragment = new ProfileUserFragment(); // Replace with your ProfileFragment
                } else if (itemId == R.id.nav_settingsUser) {
                    selectedFragment = new SettingUserFragment(); // Replace with your SettingsFragment
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_containerUser, selectedFragment)
                            .commit();
                }

                return true;
            }
        });

        // Set the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_homeUser);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_containerUser, new RaiseComplaintFragment()) // Replace with your default fragment
                    .commit();
        }
    }




    private void showNotifications() {
        // Sample notifications
        List<String> notifications = new ArrayList<>();
        notifications.add("New Ticket Created");
        notifications.add("Your Ticket has been Resolved");
        notifications.add("System Maintenance Notification");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notifications")
                .setItems(notifications.toArray(new String[0]), null)
                .setPositiveButton("OK", null);

        // Show the dialog
        builder.create().show();
    }
}
