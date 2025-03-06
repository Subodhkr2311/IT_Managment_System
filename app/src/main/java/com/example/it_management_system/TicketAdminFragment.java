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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketAdminFragment extends Fragment {

    private RecyclerView ticketsRecyclerView;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private DatabaseReference complaintsRef;
    private List<Complaints> complaintsList;
    private TicketAdapter ticketAdapter;

    // ActionMode for contextual selection UI
    private ActionMode actionMode;

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
            setupAdapterSelectionListener();
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
        // For normal click, show the ticket journey Bottom Sheet
        ticketAdapter = new TicketAdapter(complaintsList, complaint -> {
            showJourneyBottomSheet(complaint);
        });

        // Listen for selection changes to update ActionMode title.
        ticketAdapter.setOnSelectionChangedListener(count -> {
            if (actionMode != null) {
                if (count == 0) {
                    actionMode.finish();
                } else {
                    actionMode.setTitle(count + " selected");
                }
            }
        });

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

                // Sort complaints based on date (null dates moved to end)
                Collections.sort(complaintsList, (c1, c2) -> {
                    if (c1.getDate() == null && c2.getDate() == null) return 0;
                    if (c1.getDate() == null) return 1;
                    if (c2.getDate() == null) return -1;
                    return c2.getDate().compareTo(c1.getDate());
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

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

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

    private void setupAdapterSelectionListener() {
        // Start ActionMode when selection begins
        ticketAdapter.setOnSelectionChangedListener(count -> {
            if (count > 0 && actionMode == null) {
                actionMode = ((androidx.appcompat.app.AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
            }
            if (actionMode != null) {
                if (count == 0) {
                    actionMode.finish();
                } else {
                    actionMode.setTitle(count + " selected");
                }
            }
        });
    }

    // Define the ActionMode callback for multi-select
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_assign) {
                // Trigger bulk assignment via Bottom Sheet
                showBulkAssignBottomSheet(ticketAdapter.getSelectedComplaints());
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ticketAdapter.disableSelectionMode();
            actionMode = null;
        }
    };

    // Open BulkAssignBottomSheet for bulk assignment
    private void showBulkAssignBottomSheet(List<Complaints> selectedComplaints) {
        BulkAssignBottomSheet bottomSheet = BulkAssignBottomSheet.newInstance();
        bottomSheet.setOnExecutiveSelectedListener(selectedExecutive -> {
            assignTicketsToExecutive(selectedComplaints, selectedExecutive);
        });
        bottomSheet.show(getChildFragmentManager(), "BulkAssignBottomSheet");
    }

    // Updated method: Use multi-location update so that for each complaint the "assignedTo" and "status" fields are updated
    // Update selected tickets in Firebase with the chosen executive.
    private void assignTicketsToExecutive(List<Complaints> selectedComplaints, User executive) {
        if (selectedComplaints == null || selectedComplaints.isEmpty()) return;

        // Prepare a map for multi-location update.
        // Note: We update "complaintStatus" (not "status") to match your model.
        Map<String, Object> updates = new HashMap<>();
        for (Complaints complaint : selectedComplaints) {
            // Ensure complaint ID is valid
            if (complaint.getId() == null) continue;
            updates.put("complaints/" + complaint.getId() + "/assignedTo", executive.getName());
            updates.put("complaints/" + complaint.getId() + "/complaintStatus", "Assigned");
            updates.put("Users/" + executive.getId() + "/complaintIds/" + complaint.getId(), true);
        }

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Tickets assigned successfully", Toast.LENGTH_SHORT).show();
                    showAssignmentSuccessAnimation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to assign tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showAssignmentSuccessAnimation() {
        View successView = LayoutInflater.from(getContext())
                .inflate(R.layout.assignment_success_animation, null);

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(successView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 1500);
    }

    // Restore the Ticket Journey feature.
    // When a ticket is tapped (in non-selection mode), show its journey.
    private void showJourneyBottomSheet(Complaints complaint) {
        // This assumes you have a TicketJourneyBottomSheet class with a newInstance() method.
        TicketJourneyBottomSheet bottomSheet = TicketJourneyBottomSheet.newInstance(complaint, TicketJourneyBottomSheet.ROLE_ADMIN);
        bottomSheet.show(getChildFragmentManager(), "TicketJourney");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    // Helper method to show Toast messages.
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
