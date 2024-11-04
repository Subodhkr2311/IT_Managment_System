package com.example.it_management_system;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
        executivesAdapter = new ExecutivesAdapter(executivesList);

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
        String availability = selectedChip != null ? selectedChip.getText().toString() : "All";
        executivesAdapter.filter(searchQuery, availability);
    }

    private void setupAddExecutiveButton() {
        fabAddExecutive.setOnClickListener(v -> showAddExecutiveDialog());
    }

    private void showAddExecutiveDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_executive, null);
        TextInputEditText emailInput = dialogView.findViewById(R.id.email_input);

        new MaterialAlertDialogBuilder(requireContext())
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

}
