package com.example.it_management_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingExecutiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingExecutiveFragment extends Fragment {

    // Parameter arguments
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Parameters
    private String mParam1;
    private String mParam2;

    public SettingExecutiveFragment() {
        // Required empty public constructor
    }

    public static SettingExecutiveFragment newInstance(String param1, String param2) {
        SettingExecutiveFragment fragment = new SettingExecutiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_executive, container, false);

        // Initialize the logout button
        Button logoutButton = view.findViewById(R.id.logoutExecutive);

        // Set OnClickListener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logout functionality here
                // For example, clear user session data
                clearUserSession();

                // Navigate back to the login screen
                Intent intent = new Intent(getActivity(), loginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish(); // Close current activity
            }
        });

        return view;
    }

    // Method to clear user session data (implement as per your session management)
    private void clearUserSession() {
        // Clear shared preferences, database or any session data
        // Example: SharedPreferences preferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        // preferences.edit().clear().apply();
    }
}
