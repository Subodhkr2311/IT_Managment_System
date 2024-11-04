package com.example.it_management_system;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketAdminFragment extends Fragment implements TicketAdapter.OnTicketClickListener {

    private RecyclerView ticketsRecyclerView;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private DatabaseReference complaintsRef;
    private List<Complaints> complaintsList;
    private TicketAdapter ticketAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            initializeViews(view);
            setupRecyclerView();
            loadComplaints();
            setupSearchListener();
            setupFilterListener();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TicketAdminFragment", e);
            showToast("Error initializing ticket view. Please try again.");
        }
    }

    private void initializeViews(View view) {
        ticketsRecyclerView = view.findViewById(R.id.tickets_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);

        if (ticketsRecyclerView == null || searchEditText == null || filterChipGroup == null) {
            throw new IllegalStateException("One or more views not found in layout");
        }
    }

    private void setupRecyclerView() {
        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");
        complaintsList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(complaintsList, this);

        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketsRecyclerView.setAdapter(ticketAdapter);
    }

    private void loadComplaints() {
        complaintsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintsList.clear();
                int parseErrorCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Complaints complaint = snapshot.getValue(Complaints.class);
                        if (complaint != null) {
                            complaint.setId(snapshot.getKey());
                            complaintsList.add(complaint);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing complaint: " + snapshot.getKey(), e);
                        parseErrorCount++;
                    }
                }

                // Sort complaints based on the date, checking for null values
                Collections.sort(complaintsList, (c1, c2) -> {
                    // Handle null cases explicitly
                    if (c1.getDate() == null && c2.getDate() == null) return 0;
                    if (c1.getDate() == null) return 1;  // Move nulls to the end
                    if (c2.getDate() == null) return -1; // Move nulls to the end
                    return c2.getDate().compareTo(c1.getDate()); // Sort by date
                });

                ticketAdapter.updateList(complaintsList);

                if (parseErrorCount > 0) {
                    showToast("Some complaints couldn't be loaded. Please refresh.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load complaints", databaseError.toException());
                showToast("Failed to load complaints. Please try again.");
            }
        });
    }


    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });
    }

    private void setupFilterListener() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void applyFilters() {
        String searchQuery = searchEditText.getText().toString().trim().toLowerCase();
        Chip selectedChip = filterChipGroup.findViewById(filterChipGroup.getCheckedChipId());
        String filter = selectedChip != null ? selectedChip.getText().toString() : "All";
        ticketAdapter.filter(searchQuery, filter);
    }

    @Override
    public void onTicketClick(Complaints complaint) {
        showAssignDialog(complaint);
    }

    private void showAssignDialog(Complaints complaint) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query executivesQuery = usersRef.orderByChild("role").equalTo("executive");

        executivesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> executives = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User executive = snapshot.getValue(User.class);
                    if (executive != null) {
                        executive.setId(snapshot.getKey());
                        executives.add(executive);
                    }
                }

                if (executives.isEmpty()) {
                    Toast.makeText(getContext(), "No executives found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] executiveNames = new String[executives.size()];
                for (int i = 0; i < executives.size(); i++) {
                    executiveNames[i] = executives.get(i).getName();
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Assign Ticket")
                        .setItems(executiveNames, (dialog, which) -> {
                            User selectedExecutive = executives.get(which);
                            assignTicketToExecutive(complaint, selectedExecutive);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load executives", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignTicketToExecutive(Complaints complaint, User executive) {
        DatabaseReference complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaint.getId());
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(executive.getId());

        Map<String, Object> complaintUpdates = new HashMap<>();
        complaintUpdates.put("assignedTo", executive.getName());
        complaintUpdates.put("status", "Assigned");

        complaintRef.updateChildren(complaintUpdates)
                .addOnSuccessListener(aVoid -> {
                    userRef.child("complaintIds").child(complaint.getId()).setValue(true)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "Ticket assigned successfully", Toast.LENGTH_SHORT).show();
                                showAssignmentSuccessAnimation();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update executive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to assign ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAssignmentSuccessAnimation() {
        View successView = LayoutInflater.from(getContext())
                .inflate(R.layout.assignment_success_animation, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(successView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        // Dismiss dialog after animation
        new Handler().postDelayed(dialog::dismiss, 1500);
    }
}
