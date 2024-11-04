package com.example.it_management_system;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class tabComplaintsExecutive extends Fragment {

    private RecyclerView recyclerViewComplaints;
    private ComplaintsAdapterExecutive adapter;
    private DatabaseReference databaseReference;

    public tabComplaintsExecutive() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_complaints_executive, container, false);


        recyclerViewComplaints = view.findViewById(R.id.recyclerViewComplaintsExecutive);
        recyclerViewComplaints.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseReference = FirebaseDatabase.getInstance().getReference("complaints");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Complaints> complaintsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Complaints complaint = snapshot.getValue(Complaints.class);
                    if (complaint != null) {
                        complaint.setId(snapshot.getKey());

                        if (snapshot.hasChild("ticketJourney")) {
                            List<Complaints.JourneyEvent> journeyEvents = new ArrayList<>();
                            for (DataSnapshot journeyEventSnapshot : snapshot.child("ticketJourney").getChildren()) {
                                Complaints.JourneyEvent journeyEvent = journeyEventSnapshot.getValue(Complaints.JourneyEvent.class);
                                if (journeyEvent != null) {
                                    journeyEvents.add(journeyEvent);
                                }
                            }
                            complaint.setJourneyEvents(journeyEvents);
                        }
                        complaintsList.add(complaint);
                    }
                }
                // Sort the list by date in descending order (latest first)
                // Sort the list by date in descending order (latest first) with null-check
                Collections.sort(complaintsList, (c1, c2) -> {
                    if (c1.getDate() == null && c2.getDate() == null) return 0;
                    if (c1.getDate() == null) return 1;
                    if (c2.getDate() == null) return -1;
                    return c2.getDate().compareTo(c1.getDate());
                });

                adapter = new ComplaintsAdapterExecutive(complaintsList, getContext(), tabComplaintsExecutive.this);
                recyclerViewComplaints.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load complaints", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Method to handle claiming the ticket
    public void claimTicket(Complaints complaint) {
        if (complaint.getId() == null) {
            Toast.makeText(getContext(), "Invalid complaint ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (complaint.getAssignedTo() != null && !complaint.getAssignedTo().isEmpty()) {
            showAssignedDialog(complaint.getAssignedTo());
            return; //
        }

        // Create a dialog using the new layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_claim_ticket, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvClaimMessage = dialogView.findViewById(R.id.tvClaimMessage);
        Button btnClaim = dialogView.findViewById(R.id.btnClaim);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvClaimMessage.setText("Do you want to claim this ticket: " + complaint.getTitle() + "?");

        btnClaim.setOnClickListener(v -> {

            DatabaseReference complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaint.getId());
            String executiveId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            HashMap<String, Object> updates = new HashMap<>();
            updates.put("assignedTo", executiveId);
            updates.put("status", "Claimed");
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            complaint.addJourneyEvent("Ticket claimed by executive", timestamp);

            // Add ticket journey to Firebase
            updates.put("ticketJourney", complaint.getTicketJourney());


            complaintRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    complaint.setAssignedTo(executiveId);
                    complaint.setStatus("Claimed");

                    Toast.makeText(getContext(), "Ticket claimed successfully", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to claim the ticket", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show(); // Show the dialog
    }


    // Show a dialog if the ticket is already assigned
    private void showAssignedDialog(String assignedTo) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Ticket Unavailable")
                .setMessage("This ticket cannot be claimed because it is already assigned to " + assignedTo + ".")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    public void updateJourney(Complaints complaint, String event) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        complaint.addJourneyEvent(event, timestamp);

        DatabaseReference complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaint.getId());
        complaintRef.child("ticketJourney").setValue(complaint.getTicketJourney());
    }


}
