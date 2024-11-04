package com.example.it_management_system;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintsAdapterExecutive extends RecyclerView.Adapter<ComplaintsAdapterExecutive.ComplaintsViewHolder> {

    private List<Complaints> complaintsList;
    private Context context;
    private tabComplaintsExecutive fragment;

    // Declare variables for the journey dialog
    private EditText resolveReasonInput;
    private EditText expectedResolutionTime;
    private JourneyAdapter journeyAdapter;

    public ComplaintsAdapterExecutive(List<Complaints> complaintsList, Context context, tabComplaintsExecutive fragment) {
        this.complaintsList = complaintsList;
        this.context = context;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ComplaintsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_executivecomplaint, parent, false);
        return new ComplaintsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintsViewHolder holder, int position) {
        Complaints complaint = complaintsList.get(position);

        holder.title.setText(complaint.getTitle());
        holder.date.setText(complaint.getDate());
        holder.location.setText(complaint.getLocation());

        holder.itemView.setOnLongClickListener(v -> {
            showJourneyDialog(complaintsList.get(position));
            return true;
        });
        holder.status.setText(complaint.getStatus());
        if ("Claimed".equals(complaint.getStatus())) {
            holder.status.setTextColor(Color.GREEN); // Example: green for claimed tickets
        } else {
            holder.status.setTextColor(Color.RED); // Example: red for other statuses
        }

        // Show description only if available
        if (complaint.getDescription() != null && !complaint.getDescription().isEmpty()) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(complaint.getDescription());
        } else {
            holder.description.setVisibility(View.GONE);
        }

        // Click listener for the claim action
        holder.itemView.setOnClickListener(v -> fragment.claimTicket(complaint));
    }

    @Override
    public int getItemCount() {
        return complaintsList.size();
    }

    public static class ComplaintsViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, location, status, description;

        public ComplaintsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            location = itemView.findViewById(R.id.tvLocation);
            status = itemView.findViewById(R.id.tvStatus);
            description = itemView.findViewById(R.id.tvDescription);
        }
    }

    private void showJourneyDialog(Complaints complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ticket_journey, null);
        builder.setView(dialogView);

        RecyclerView rvJourney = dialogView.findViewById(R.id.rvJourney);
        rvJourney.setLayoutManager(new LinearLayoutManager(context));

        List<Complaints.JourneyEvent> journeyEvents = complaint.getTicketJourney();
        journeyAdapter = new JourneyAdapter(journeyEvents);
        rvJourney.setAdapter(journeyAdapter);

        Button btnArriving = dialogView.findViewById(R.id.btnArriving);
        Button btnResolve = dialogView.findViewById(R.id.btnResolve);
        Button btnResolveYes = dialogView.findViewById(R.id.btnResolveYes);
        Button btnResolveNo = dialogView.findViewById(R.id.btnResolveNo);
        TextView resolveReasonSection = dialogView.findViewById(R.id.resolveReasonSection);
        resolveReasonInput = dialogView.findViewById(R.id.resolveReasonInput);
        expectedResolutionTime = dialogView.findViewById(R.id.expectedResolutionTime);

        // Initially hide resolve options
        btnResolveYes.setVisibility(View.GONE);
        btnResolveNo.setVisibility(View.GONE);
        resolveReasonSection.setVisibility(View.GONE);
        expectedResolutionTime.setVisibility(View.GONE);

        // Hide the Arriving button if ticket is resolved
        btnArriving.setVisibility("Resolved".equals(complaint.getStatus()) ? View.GONE : View.VISIBLE);

        btnArriving.setOnClickListener(v -> {
            // Show TimePicker for arriving
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                String selectedTime = hourOfDay + "h " + minute + "m";
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                complaint.addJourneyEvent("Arriving in " + selectedTime, timestamp);

                // Save to Firebase
                DatabaseReference complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaint.getId());
                complaintRef.child("ticketJourney").setValue(complaint.getTicketJourney());

                // Show Mark as Resolved button and hide Arriving button
                btnResolve.setVisibility(View.VISIBLE);
                btnArriving.setVisibility(View.GONE);
                journeyAdapter.notifyDataSetChanged();
            }, 0, 0, true);
            timePickerDialog.setTitle("Select Arrival Time");
            timePickerDialog.show();
        });

        btnResolve.setOnClickListener(v -> {
            btnResolveYes.setVisibility(View.VISIBLE);
            btnResolveNo.setVisibility(View.VISIBLE);
            btnResolve.setVisibility(View.GONE);
        });

        btnResolveYes.setOnClickListener(v -> {
            String executiveName = "Executive Name"; // Replace with actual executive's name
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            complaint.addJourneyEvent("Ticket resolved by executive: " + executiveName, timestamp);

            // Update Firebase
            DatabaseReference complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaint.getId());
            complaintRef.child("ticketJourney").setValue(complaint.getTicketJourney());
            complaintRef.child("status").setValue("Resolved");

            playCongratulationAnimation();

            // Hide all buttons and show only the journey details
            btnArriving.setVisibility(View.GONE);
            btnResolveYes.setVisibility(View.GONE);
            btnResolveNo.setVisibility(View.GONE);
            resolveReasonSection.setVisibility(View.GONE);
            expectedResolutionTime.setVisibility(View.GONE);

            journeyAdapter.notifyDataSetChanged();
        });

        btnResolveNo.setOnClickListener(v -> {
            resolveReasonSection.setVisibility(View.VISIBLE);
            btnResolveYes.setVisibility(View.GONE);
            btnResolveNo.setVisibility(View.GONE);
            resolveReasonInput.setVisibility(View.VISIBLE);
            expectedResolutionTime.setVisibility(View.VISIBLE);
            showExpectedResolutionTimePicker(expectedResolutionTime, complaint, btnArriving); // Pass btnArriving
        });

        builder.setTitle("Ticket Journey")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showExpectedResolutionTimePicker(EditText expectedResolutionTime, Complaints complaint, Button btnArriving) {
        expectedResolutionTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                String resolutionTime = hourOfDay + ":" + minute;
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                String reason = resolveReasonInput.getText().toString();

                complaint.addJourneyEvent("Resolve failed: " + reason + ". Expected to be resolved by " + resolutionTime, timestamp);

                // Update Firebase
                DatabaseReference complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaint.getId());
                complaintRef.child("ticketJourney").setValue(complaint.getTicketJourney());

                // Hide both input fields after setting expected time
                resolveReasonInput.setVisibility(View.GONE);
                expectedResolutionTime.setVisibility(View.GONE);
                btnArriving.setVisibility(View.VISIBLE); // Show Mark Arriving option
                journeyAdapter.notifyDataSetChanged();
            }, 0, 0, true);
            timePickerDialog.setTitle("Expected Resolution Time");
            timePickerDialog.show();
        });
    }

    private void playCongratulationAnimation() {
        // Your animation code here
        Toast.makeText(context, "Congratulations! Ticket resolved successfully!", Toast.LENGTH_SHORT).show();
        // You can replace this Toast with an actual animation or dialog
    }
}
