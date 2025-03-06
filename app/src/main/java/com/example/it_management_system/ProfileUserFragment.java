package com.example.it_management_system;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileUserFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private HistoryAdapter historyAdapter;
    private ArrayList<RequirementModel> requirementList = new ArrayList<>();
    private EditText itemEditText, itemCountEditText, locationEditText, fromDateTimeEditText, toDateTimeEditText;
    private Button submitRequirementButton;
    private RecyclerView recyclerViewHistory;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd ( HH:mm )", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        itemEditText = view.findViewById(R.id.itemEditTextUser);
        itemCountEditText = view.findViewById(R.id.itemCountEditTextUser);
        locationEditText = view.findViewById(R.id.locationEditTextUser);
        fromDateTimeEditText = view.findViewById(R.id.fromDateTimeEditTextUser);
        toDateTimeEditText = view.findViewById(R.id.toDateTimeEditTextUser);
        submitRequirementButton = view.findViewById(R.id.submitRequirementButtonUser);
        recyclerViewHistory = view.findViewById(R.id.recycler_view_history);

        // Setup RecyclerView
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(requirementList);
        recyclerViewHistory.setAdapter(historyAdapter);

        // Load existing requirements history
        loadRequirementHistory();

        fromDateTimeEditText.setOnClickListener(v -> showDateTimePicker(fromDateTimeEditText, true));
        toDateTimeEditText.setOnClickListener(v -> showDateTimePicker(toDateTimeEditText, false));

        submitRequirementButton.setOnClickListener(v -> submitRequirement());
    }

    private void showDateTimePicker(EditText editText, boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                if (isFromDate) {
                                    // Set selected from date and enable toDate field
                                    fromDateTimeEditText.setText(dateFormat.format(calendar.getTime()));
                                    toDateTimeEditText.setEnabled(true);
                                } else {
                                    // Check if toDate is after fromDate
                                    String fromDateStr = fromDateTimeEditText.getText().toString();
                                    try {
                                        if (!fromDateStr.isEmpty() &&
                                                dateFormat.parse(fromDateStr).compareTo(calendar.getTime()) <= 0) {
                                            toDateTimeEditText.setText(dateFormat.format(calendar.getTime()));
                                        } else {
                                            Toast.makeText(getContext(),
                                                    "To date should be after From date",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // For "From Date" picker, restrict past dates
        if (isFromDate) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        } else {
            // For "To Date" picker, set minimum date and time to "From Date"
            String fromDateStr = fromDateTimeEditText.getText().toString();
            if (!fromDateStr.isEmpty()) {
                try {
                    calendar.setTime(dateFormat.parse(fromDateStr));
                    datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // Show the DatePickerDialog after all configurations are set
        datePickerDialog.show();
    }

    private void submitRequirement() {
        String item = itemEditText.getText().toString().trim();
        String itemCount = itemCountEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String fromDate = fromDateTimeEditText.getText().toString();
        String toDate = toDateTimeEditText.getText().toString();

        if (item.isEmpty() || itemCount.isEmpty() || location.isEmpty() ||
                fromDate.isEmpty() || toDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child("Users").child(userId).child("name").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String userName = task.getResult().getValue(String.class);
                        if (userName == null || userName.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "Please set your name in profile settings",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        String requirementId = mDatabase.child("requirements").push().getKey();
                        if (requirementId == null) {
                            Toast.makeText(getContext(),
                                    "Error generating requirement ID",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RequirementModel requirement = new RequirementModel(
                                item, location, fromDate, toDate, itemCount, userName, userId
                        );

                        // Create a map for the batch update
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("/requirements/" + requirementId, requirement);
                        updates.put("/Users/" + userId + "/requirements/" + requirementId, true);

                        mDatabase.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(),
                                            "Requirement submitted successfully",
                                            Toast.LENGTH_SHORT).show();
                                    clearForm();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Submission failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(getContext(),
                                "Failed to get user data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearForm() {
        itemEditText.setText("");
        itemCountEditText.setText("");
        locationEditText.setText("");
        fromDateTimeEditText.setText("");
        toDateTimeEditText.setText("");
    }


    private void loadRequirementHistory() {
        String userId = mAuth.getCurrentUser().getUid();

        // Get all requirements IDs from user's node
        mDatabase.child("Users").child(userId).child("requirements")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requirementList.clear();

                        // For tracking when all requirements are loaded
                        final int[] totalRequirements = {(int) snapshot.getChildrenCount()};
                        final int[] loadedRequirements = {0};

                        if (totalRequirements[0] == 0) {
                            historyAdapter.notifyDataSetChanged();
                            return;
                        }

                        // For each requirement ID in user's node
                        for (DataSnapshot requirementSnapshot : snapshot.getChildren()) {
                            String requirementId = requirementSnapshot.getKey();

                            // Get requirement details from main requirements node
                            mDatabase.child("requirements").child(requirementId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            RequirementModel requirement = dataSnapshot.getValue(RequirementModel.class);
                                            if (requirement != null) {
                                                requirementList.add(requirement);
                                            }

                                            loadedRequirements[0]++;

                                            // When all requirements are loaded, sort and update UI
                                            if (loadedRequirements[0] == totalRequirements[0]) {
                                                // Sort by date (newest first)
                                                // Sort by date (newest first)
                                                Collections.sort(requirementList, (r1, r2) -> {
                                                    try {
                                                        // Check if dates are null
                                                        String date1 = r1.getFromDate();
                                                        String date2 = r2.getFromDate();

                                                        if (date1 == null && date2 == null) return 0;
                                                        if (date1 == null) return 1;
                                                        if (date2 == null) return -1;

                                                        return dateFormat.parse(date2).compareTo(dateFormat.parse(date1));
                                                    } catch (ParseException e) {
                                                        // If parsing fails, consider them equal in the sort order
                                                        return 0;
                                                    }
                                                });
                                                historyAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            loadedRequirements[0]++;
                                            Toast.makeText(getContext(),
                                                    "Error loading requirement: " + error.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),
                                "Failed to load history: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}


