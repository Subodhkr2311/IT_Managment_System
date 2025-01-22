package com.example.it_management_system; // Change to your package name

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;

import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.mlkit.nl.translate.Translator;
import android.widget.RadioButton;

public class SettingUserFragment extends Fragment {

    private Switch switchNotifications;
    private Spinner languageSpinner;
    private RadioGroup themeRadioGroup;
    private Button buttonFeedback, buttonLogout;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_user, container, false);

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);

        // Find Views
        switchNotifications = view.findViewById(R.id.switch_notifications);
        languageSpinner = view.findViewById(R.id.language_spinner);
        themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        buttonFeedback = view.findViewById(R.id.button_feedback);
        buttonLogout = view.findViewById(R.id.button_logout);

        // Load saved settings
        loadSettings();
        setupLanguageSpinner();

        // Set OnClickListener for feedback button
        buttonFeedback.setOnClickListener(v -> {
            // Handle feedback functionality (e.g., open feedback activity)
            Toast.makeText(getContext(), "Feedback button clicked", Toast.LENGTH_SHORT).show();
        });

        // Set OnClickListener for logout button
        buttonLogout.setOnClickListener(v -> {
            // Handle logout functionality
            Toast.makeText(getContext(), "Logout button clicked", Toast.LENGTH_SHORT).show();
            // Clear session data if needed (example: clear user token or data)
            // Redirect to LoginActivity
            Intent intent = new Intent(getActivity(), loginActivity.class);
            startActivity(intent);
            getActivity().finish(); // Optional: finish current activity
        });

        // Set OnCheckedChangeListener for notification switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });

        // Set OnCheckedChangeListener for theme radio group
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedTheme = checkedId == R.id.radio_light ? "Light" : "Dark";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("selected_theme", selectedTheme);
            editor.apply();

            // Restart the activity to apply theme changes
            requireActivity().recreate();
        });


        return view;
    }

    private void loadSettings() {
        // Load settings from SharedPreferences
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        String selectedTheme = sharedPreferences.getString("selected_theme", "Light");
        if ("Light".equals(selectedTheme)) {
            themeRadioGroup.check(R.id.radio_light);
        } else {
            themeRadioGroup.check(R.id.radio_dark);
        }
    }

    private void setupLanguageSpinner() {
        String[] languages = {"English", "Hindi", "Spanish"}; // Add your languages here
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
    }
}
