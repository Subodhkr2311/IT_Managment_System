package com.example.it_management_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingAdminFragment extends Fragment {

    private Switch switchNotifications;
    private Spinner languageSpinner;
    private RadioGroup themeRadioGroup;
    private Button buttonFeedback, buttonLogout;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_admin, container, false);

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("Settings", getContext().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Initialize Views
        switchNotifications = view.findViewById(R.id.switch_notifications);
        languageSpinner = view.findViewById(R.id.language_spinner);
        themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        buttonFeedback = view.findViewById(R.id.button_feedback);
        buttonLogout = view.findViewById(R.id.button_logout);

        // Set Notification Switch listener
        switchNotifications.setChecked(sharedPreferences.getBoolean("notifications_enabled", true));  // Load saved state
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
            Toast.makeText(getContext(), "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Language Spinner listener
        // (Assume you have an adapter for languages in the spinner)
        languageSpinner.setSelection(sharedPreferences.getInt("language_selected", 0));
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("language_selected", position);
                editor.apply();
                Toast.makeText(getContext(), "Language changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        // Theme Radio Group listener
        String savedTheme = sharedPreferences.getString("theme", "Light");
        if (savedTheme.equals("Light")) {
            ((RadioButton) themeRadioGroup.findViewById(R.id.radio_light)).setChecked(true);
        } else {
            ((RadioButton) themeRadioGroup.findViewById(R.id.radio_dark)).setChecked(true);
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String theme = checkedId == R.id.radio_light ? "Light" : "Dark";
            editor.putString("theme", theme);
            editor.apply();
            Toast.makeText(getContext(), "Theme set to " + theme, Toast.LENGTH_SHORT).show();
        });

        // Feedback Button listener
        buttonFeedback.setOnClickListener(v -> {
            // Handle feedback action (you could start an activity or open a dialog here)
            Toast.makeText(getContext(), "Feedback button clicked", Toast.LENGTH_SHORT).show();
        });

        // Logout Button listener
        buttonLogout.setOnClickListener(v -> {
            // Handle logout action (you could clear user data and return to a login screen)
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
            // For example, start login activity:
            Intent intent = new Intent(getActivity(), loginActivity.class);
            startActivity(intent);
            getActivity().finish();  // Close current activity
        });

        return view;
    }
}
