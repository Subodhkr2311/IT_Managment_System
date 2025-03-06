package com.example.it_management_system;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingExecutiveFragment extends Fragment {

    // Parameter arguments
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Parameters
    private String mParam1;
    private String mParam2;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_executive, container, false);

        // Initialize the logout button
        Button logoutButton = view.findViewById(R.id.logoutExecutive);
        logoutButton.setOnClickListener(v -> {
            clearUserSession();
            Intent intent = new Intent(getActivity(), loginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish(); // Close current activity
        });

        // Set up the Attendance RadioGroup
        RadioGroup radioGroupAttendance = view.findViewById(R.id.radioGroupAttendance);
        radioGroupAttendance.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAbsent) {
                showLeaveRequestBottomSheet();
            } else if (checkedId == R.id.radioPresent) {
                // Optionally, update availability status in the database here
                Toast.makeText(getContext(), "Marked as Present", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Method to clear user session data (implement as per your session management)
    private void clearUserSession() {
        // Clear shared preferences, database or any session data
    }

    // Show a Bottom Sheet to fill leave request details when the executive marks "Absent"
    private void showLeaveRequestBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_leave_request, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText etReason = sheetView.findViewById(R.id.etReason);
        Button btnSubmit = sheetView.findViewById(R.id.btnSubmit);
        Button btnCancel = sheetView.findViewById(R.id.btnCancel);

        btnSubmit.setOnClickListener(v -> {
            String reason = etReason.getText().toString().trim();
            if (TextUtils.isEmpty(reason)) {
                Toast.makeText(getContext(), "Please enter a reason", Toast.LENGTH_SHORT).show();
            } else {
                submitLeaveRequest(reason);
                bottomSheetDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // Optionally, revert the selection back to "Present"
            RadioButton radioPresent = getView().findViewById(R.id.radioPresent);
            radioPresent.setChecked(true);
        });

        bottomSheetDialog.show();
    }

    // Submit the leave request (e.g., to Firebase) with a status "pending"
    private void submitLeaveRequest(String reason) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        long currentTime = System.currentTimeMillis();
        LeaveRequest leaveRequest = new LeaveRequest(userId, reason, currentTime, "pending");

        String leaveId = databaseReference.child("leave_requests").push().getKey();
        if (leaveId != null) {
            databaseReference.child("leave_requests").child(leaveId).setValue(leaveRequest)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Leave request submitted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to submit leave request", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
