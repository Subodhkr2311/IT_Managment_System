package com.example.it_management_system;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class RequestRequirementsFragmentAdmin extends Fragment {

    private EditText itemEditText, itemCountEditText, locationEditText;
    private EditText fromDateTimeEditText, toDateTimeEditText;
    private Button submitRequirementButton;
    private RecyclerView requirementsRecyclerView;

    private DatabaseReference requirementsDatabaseRef;
    private ArrayList<RequirementModel> requirementsList;
    private RequirementRecyclerAdapterAdmin recyclerAdapter;
    private Calendar fromDateCalendar;
    public RequestRequirementsFragmentAdmin() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requirementsDatabaseRef = FirebaseDatabase.getInstance().getReference("requirements");
        requirementsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_requirements_admin, container, false);

        itemEditText = view.findViewById(R.id.itemEditText);
        itemCountEditText = view.findViewById(R.id.itemCountEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        fromDateTimeEditText = view.findViewById(R.id.fromDateTimeEditText);
        toDateTimeEditText = view.findViewById(R.id.toDateTimeEditText);
        submitRequirementButton = view.findViewById(R.id.submitRequirementButton);

        // Set up RecyclerView
        requirementsRecyclerView = view.findViewById(R.id.requirementsRecyclerViewAdmin);
        requirementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapter = new RequirementRecyclerAdapterAdmin(getContext(), requirementsList);
        requirementsRecyclerView.setAdapter(recyclerAdapter);

        fromDateTimeEditText.setOnClickListener(v -> showDateTimePicker(fromDateTimeEditText, null));
        toDateTimeEditText.setOnClickListener(v -> {
            Calendar fromCalendar = Calendar.getInstance();
            if (!fromDateTimeEditText.getText().toString().isEmpty()) {
                // Parse the "From Date" to set it as the minimum date for "To Date"
                try {
                    String fromDateTime = fromDateTimeEditText.getText().toString();
                    fromCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd ( HH:mm )", Locale.getDefault()).parse(fromDateTime));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            showDateTimePicker(toDateTimeEditText, fromCalendar);
        });
        submitRequirementButton.setOnClickListener(v -> submitRequirement());

        loadRequirements();

        return view;
    }

    private void showDateTimePicker(final EditText editText, Calendar minDate) {
        final Calendar calendar = Calendar.getInstance();

        // Open DatePicker first
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            // Open TimePicker after selecting the date
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, selectedHour, selectedMinute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                String dateTimeFormat = "yyyy-MM-dd ( HH:mm )";
                SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat, Locale.getDefault());
                editText.setText(sdf.format(calendar.getTime()));

                // Logic to set 'fromDateCalendar' when 'From Date' is set
                if (editText == fromDateTimeEditText) {
                    fromDateCalendar = (Calendar) calendar.clone(); // Save the 'from date' to set as min date for 'to date'
                    toDateTimeEditText.setEnabled(true); // Enable 'to date' field after 'from date' is selected
                } else if (editText == toDateTimeEditText) {
                    // Ensure 'To Date' is after 'From Date'
                    if (fromDateCalendar != null) {
                        // Set minimum date and time for To Date to be greater than From Date
                        if (calendar.compareTo(fromDateCalendar) <= 0) {
                            Toast.makeText(getContext(), "'To Date' must be after 'From Date'", Toast.LENGTH_SHORT).show();
                            toDateTimeEditText.setText(""); // Clear the invalid To Date
                            return;
                        }
                    }
                }

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            // Show the TimePicker
            timePickerDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date for DatePicker if provided
        if (minDate != null) {
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        } else {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        datePickerDialog.show();
    }

    private void submitRequirement() {
        String item = itemEditText.getText().toString().trim();
        String count = itemCountEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String fromDateTime = fromDateTimeEditText.getText().toString().trim();
        String toDateTime = toDateTimeEditText.getText().toString().trim();

        if (item.isEmpty() || count.isEmpty() || location.isEmpty() || fromDateTime.isEmpty() || toDateTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optional: Validate that "From Date" is before "To Date"
        try {
            Calendar fromCalendar = Calendar.getInstance();
            Calendar toCalendar = Calendar.getInstance();
            fromCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd ( HH:mm )", Locale.getDefault()).parse(fromDateTime));
            toCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd ( HH:mm )", Locale.getDefault()).parse(toDateTime));
            if (fromCalendar.after(toCalendar)) {
                Toast.makeText(getContext(), "'From Date' must be before 'To Date'", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = requirementsDatabaseRef.push().getKey();
        RequirementModel requirement = new RequirementModel(item, location, fromDateTime, toDateTime, count, "Admin",  id);
        requirementsDatabaseRef.child(id).setValue(requirement).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Requirement submitted", Toast.LENGTH_SHORT).show();
                clearFields();
                loadRequirements();
            } else {
                Toast.makeText(getContext(), "Failed to submit requirement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRequirements() {
        requirementsDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requirementsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RequirementModel requirement = dataSnapshot.getValue(RequirementModel.class);
                    if (requirement != null) {
                        requirementsList.add(requirement);
                    }
                }
                Collections.reverse(requirementsList); // Reverse to show most recent at the top
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load requirements", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        itemEditText.setText("");
        itemCountEditText.setText("");
        locationEditText.setText("");
        fromDateTimeEditText.setText("");
        toDateTimeEditText.setText("");
    }
}
