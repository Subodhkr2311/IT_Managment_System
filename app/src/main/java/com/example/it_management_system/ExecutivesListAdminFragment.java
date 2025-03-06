package com.example.it_management_system;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ExecutivesListAdminFragment extends Fragment {

    private RecyclerView executivesRecyclerView;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private FloatingActionButton fabAddExecutive;
    private DatabaseReference usersRef;
    private List<User> executivesList;
    private ExecutivesAdapter executivesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_executives, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        executivesRecyclerView = view.findViewById(R.id.executives_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        fabAddExecutive = view.findViewById(R.id.fab_add_executive);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        executivesList = new ArrayList<>();
        // Pass the click listener to open the bottom sheet detail view
        executivesAdapter = new ExecutivesAdapter(executivesList, executive -> showExecutiveDetailBottomSheet(executive));

        executivesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        executivesRecyclerView.setAdapter(executivesAdapter);

        loadExecutives();
        setupSearchListener();
        setupFilterListener();
        setupAddExecutiveButton();
    }

    private void loadExecutives() {
        Query executivesQuery = usersRef.orderByChild("role").equalTo("executive");
        executivesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                executivesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(snapshot.getKey()); // Set the user ID
                        executivesList.add(user);
                    }
                }
                executivesAdapter.updateList(executivesList);
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load executives: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
        String availability = selectedChip != null ? selectedChip.getText().toString() : "All";
        // Implement filtering logic in your adapter (for now, call a filter method)
        executivesAdapter.filter(searchQuery, availability);
    }

    private void setupAddExecutiveButton() {
        fabAddExecutive.setOnClickListener(v -> showAddExecutiveDialog());
    }

    private void showAddExecutiveDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_executive, null);
        TextInputEditText emailInput = dialogView.findViewById(R.id.email_input);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Executive")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!email.isEmpty()) {
                        updateUserRole(email);
                    } else {
                        Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserRole(String email) {
        Query query = usersRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean userUpdated = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userId = snapshot.getKey();
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if ("executive".equals(user.getRole())) {
                                Toast.makeText(getContext(), "User is already an executive", Toast.LENGTH_SHORT).show();
                            } else {
                                DatabaseReference userRef = usersRef.child(userId);
                                userRef.child("role").setValue("executive")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "User role updated to executive", Toast.LENGTH_SHORT).show();
                                            loadExecutives();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update user role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                            userUpdated = true;
                            break;
                        }
                    }
                    if (!userUpdated) {
                        Toast.makeText(getContext(), "User found but could not be updated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "User not found. Please check the email address.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Opens a Bottom Sheet when an executive is clicked to display detailed info and any pending leave request
    private void showExecutiveDetailBottomSheet(User executive) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_executive_detail, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvName = sheetView.findViewById(R.id.tvExecutiveName);
        TextView tvEmail = sheetView.findViewById(R.id.tvExecutiveEmail);
        TextView tvLeaveHeader = sheetView.findViewById(R.id.tvLeaveRequestHeader);
        TextView tvLeaveReason = sheetView.findViewById(R.id.tvLeaveReason);
        LinearLayout llLeaveActions = sheetView.findViewById(R.id.llLeaveActions);
        Button btnApproveLeave = sheetView.findViewById(R.id.btnApproveLeave);
        Button btnRejectLeave = sheetView.findViewById(R.id.btnRejectLeave);

        tvName.setText(executive.getName());
        tvEmail.setText(executive.getEmail());

        // Query for any pending leave request for this executive
        DatabaseReference leaveRef = FirebaseDatabase.getInstance().getReference("leave_requests");
        Query query = leaveRef.orderByChild("userId").equalTo(executive.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LeaveRequest pendingRequest = null;
                String leaveKey = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    LeaveRequest request = child.getValue(LeaveRequest.class);
                    if (request != null && "pending".equals(request.getStatus())) {
                        pendingRequest = request;
                        leaveKey = child.getKey();
                        break;
                    }
                }
                if (pendingRequest != null) {
                    // Show leave request details
                    tvLeaveHeader.setVisibility(View.VISIBLE);
                    tvLeaveReason.setVisibility(View.VISIBLE);
                    llLeaveActions.setVisibility(View.VISIBLE);
                    tvLeaveReason.setText(pendingRequest.getReason());
                    final String finalLeaveKey = leaveKey;
                    btnApproveLeave.setOnClickListener(v -> {
                        updateLeaveStatus(finalLeaveKey, "approved");
                        Toast.makeText(getContext(), "Leave Approved", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    });
                    btnRejectLeave.setOnClickListener(v -> {
                        updateLeaveStatus(finalLeaveKey, "rejected");
                        Toast.makeText(getContext(), "Leave Rejected", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    });
                } else {
                    // Hide leave section if no pending request
                    tvLeaveHeader.setVisibility(View.GONE);
                    tvLeaveReason.setVisibility(View.GONE);
                    llLeaveActions.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading leave request", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    // Update the leave request status in Firebase
    private void updateLeaveStatus(String leaveId, String status) {
        if (leaveId != null) {
            DatabaseReference leaveRef = FirebaseDatabase.getInstance().getReference("leave_requests");
            leaveRef.child(leaveId).child("status").setValue(status)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Leave " + status, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error updating leave status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
