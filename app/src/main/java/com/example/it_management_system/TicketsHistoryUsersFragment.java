package com.example.it_management_system;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketsHistoryUsersFragment extends Fragment {
    private static final String TAG = "TicketsHistoryUsers";
    private RecyclerView recyclerView;
    private SearchView searchView;
    private RadioGroup filterRadioGroup;
    private List<Complaints> complaintList;
    private ComplaintAdapter complaintAdapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets_history_users, container, false);

        initializeViews(view);
        setupFirebase();
        setupSearchView();
        setupFilterRadioGroup();
        loadUserComplaints();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_View);
        searchView = view.findViewById(R.id.search_View);
        filterRadioGroup = view.findViewById(R.id.filter_Radio_group); // Add this line

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        complaintList = new ArrayList<>();
        complaintAdapter = new ComplaintAdapter(complaintList);
        recyclerView.setAdapter(complaintAdapter);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("complaints");
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterComplaints(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterComplaints(newText);
                return true;
            }
        });
    }

    private void setupFilterRadioGroup() {
        filterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            loadUserComplaints();
        });
    }

    private void loadUserComplaints() {
        if (userId == null) return;

        Query query = databaseReference.orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Complaints complaint = snapshot.getValue(Complaints.class);
                        if (complaint != null && shouldIncludeComplaint(complaint)) {
                            complaintList.add(complaint);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing complaint: " + e.getMessage());
                    }
                }
                Collections.sort(complaintList, (c1, c2) -> c2.getDate().compareTo(c1.getDate()));
                complaintAdapter.updateList(complaintList);

                if (complaintList.isEmpty()) {
                    Toast.makeText(getContext(), "No complaints found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load complaints", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean shouldIncludeComplaint(Complaints complaint) {
        int selectedFilterId = filterRadioGroup.getCheckedRadioButtonId();
        String complaintStatus = complaint.getStatus();

        if (selectedFilterId == R.id.filter_Pending) {
            return "Pending".equals(complaintStatus);
        } else if (selectedFilterId == R.id.filter_Resolved) {
            return "Resolved".equals(complaintStatus);
        }
        return true; // Show all by default
    }

    private void filterComplaints(String query) {
        List<Complaints> filteredList = new ArrayList<>();
        for (Complaints complaint : complaintList) {
            if (matchesSearchCriteria(complaint, query.toLowerCase())) {
                filteredList.add(complaint);
            }
        }
        complaintAdapter.updateList(filteredList);
    }

    private boolean matchesSearchCriteria(Complaints complaint, String query) {
        return (complaint.getTitle() != null && complaint.getTitle().toLowerCase().contains(query)) ||
                (complaint.getDescription() != null && complaint.getDescription().toLowerCase().contains(query)) ||
                (complaint.getLocation() != null && complaint.getLocation().toLowerCase().contains(query)) ||
                (complaint.getDate() != null && complaint.getDate().toLowerCase().contains(query));
    }
}