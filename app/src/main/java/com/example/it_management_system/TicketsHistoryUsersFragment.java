package com.example.it_management_system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TicketsHistoryUsersFragment extends Fragment {
    private static final String TAG = "TicketsHistoryUsers";
    private static final String PREFS_NAME = "ClearedComplaintsPrefs";
    private static final String KEY_CLEARED_COMPLAINTS = "cleared_complaints";

    private RecyclerView recyclerView;
    private SearchView searchView;
    private RadioGroup filterRadioGroup;
    private MaterialButton btnLoadMore, btnClearHistory, btnViewDeleted;
    private List<Complaints> complaintList;         // Currently displayed complaints (current page)
    private List<Complaints> allComplaintList;      // All filtered complaints (within 7 days)
    private ComplaintAdapter complaintAdapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;

    // Pagination variables
    private final int PAGE_SIZE = 10;
    private int currentPage = 1; // start with page 1

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
        filterRadioGroup = view.findViewById(R.id.filter_Radio_group);
        btnLoadMore = view.findViewById(R.id.btnLoadMore);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        btnViewDeleted = view.findViewById(R.id.btnViewDeleted);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        complaintList = new ArrayList<>();
        allComplaintList = new ArrayList<>();
        complaintAdapter = new ComplaintAdapter(complaintList);
        recyclerView.setAdapter(complaintAdapter);

        btnLoadMore.setOnClickListener(v -> loadMoreComplaints());
        btnClearHistory.setOnClickListener(v -> clearSelectedComplaints());

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
        filterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> loadUserComplaints());
    }

    /**
     * Loads all complaints for the user from Firebase, filters out cleared complaints and those not in the last 7 days,
     * and then resets pagination.
     */
    private void loadUserComplaints() {
        if (userId == null) return;

        Query query = databaseReference.orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {  // Single fetch
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allComplaintList.clear();
                Set<String> clearedIds = getClearedComplaintIds();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Complaints complaint = snapshot.getValue(Complaints.class);
                        if (complaint != null) {
                            // Set the Firebase key as the complaint's id.
                            complaint.setId(snapshot.getKey());
                            // Only add if not cleared and meets filter criteria.
                            if (!clearedIds.contains(complaint.getId()) && shouldIncludeComplaint(complaint)) {
                                allComplaintList.add(complaint);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing complaint: " + e.getMessage());
                    }
                }
                // Sort descending by date (assuming date string format "yyyy-MM-dd")
                Collections.sort(allComplaintList, (c1, c2) -> {
                    if (c1.getDate() == null || c2.getDate() == null) return 0;
                    return c2.getDate().compareTo(c1.getDate());
                });
                // Reset pagination
                currentPage = 1;
                updatePagination();

                if (allComplaintList.isEmpty()) {
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

    /**
     * Determines if a complaint should be included based on the selected filter and if it's raised within the last 7 days.
     */
    private boolean shouldIncludeComplaint(Complaints complaint) {
        if (!isWithin7Days(complaint)) {
            return false;
        }
        int selectedFilterId = filterRadioGroup.getCheckedRadioButtonId();
        String complaintStatus = complaint.getStatus();
        if (selectedFilterId == R.id.filter_Pending) {
            return "Pending".equals(complaintStatus);
        } else if (selectedFilterId == R.id.filter_Resolved) {
            return "Resolved".equals(complaintStatus);
        }
        // "All" and "Recent" will show the complaint.
        return true;
    }

    /**
     * Checks if a complaint's date is within the last 60 days.
     * Assumes complaint.getDate() returns a string in "yyyy-MM-dd" format.
     */
    private boolean isWithin7Days(Complaints complaint) {
        if (complaint.getDate() == null) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date complaintDate = sdf.parse(complaint.getDate());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -60);
            Date sevenDaysAgo = calendar.getTime();
            return complaintDate != null && complaintDate.after(sevenDaysAgo);
        } catch (ParseException e) {
            Log.e(TAG, "Date parse error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the currently displayed list based on pagination.
     */
    private void updatePagination() {
        int endIndex = currentPage * PAGE_SIZE;
        if (endIndex > allComplaintList.size()) {
            endIndex = allComplaintList.size();
            btnLoadMore.setEnabled(false);
        } else {
            btnLoadMore.setEnabled(true);
        }
        complaintList = new ArrayList<>(allComplaintList.subList(0, endIndex));
        complaintAdapter.updateList(complaintList);
        Log.d(TAG, "Page " + currentPage + " loaded with " + complaintList.size() + " items.");
    }

    /**
     * Loads the next page of complaints.
     */
    private void loadMoreComplaints() {
        if (complaintList.size() < allComplaintList.size()) {
            currentPage++;
            updatePagination();
        } else {
            Toast.makeText(getContext(), "No more tickets to load", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Filters the current list (based on search query) locally.
     */
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

    /**
     * Clears (hides) all selected complaints from the history fragment.
     * Their IDs are stored in SharedPreferences so they remain hidden across app restarts.
     */
    private void clearSelectedComplaints() {
        List<Complaints> selectedComplaints = complaintAdapter.getSelectedComplaints();
        if (selectedComplaints.isEmpty()) {
            Toast.makeText(getContext(), "No tickets selected", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Complaints complaint : selectedComplaints) {
            if (complaint.getId() != null) {
                addClearedComplaintId(complaint.getId());
            }
        }
        allComplaintList.removeAll(selectedComplaints);
        updatePagination();
        Toast.makeText(getContext(), "Selected tickets hidden from history", Toast.LENGTH_SHORT).show();
    }

    // SharedPreferences helper methods
    private Set<String> getClearedComplaintIds() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_CLEARED_COMPLAINTS, new HashSet<>()));
    }

    private void addClearedComplaintId(String id) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> cleared = new HashSet<>(prefs.getStringSet(KEY_CLEARED_COMPLAINTS, new HashSet<>()));
        cleared.add(id);
        prefs.edit().putStringSet(KEY_CLEARED_COMPLAINTS, cleared).apply();
    }

    // (Optional) To restore an item, remove its ID from SharedPreferences.
    private void removeClearedComplaintId(String id) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> cleared = new HashSet<>(prefs.getStringSet(KEY_CLEARED_COMPLAINTS, new HashSet<>()));
        cleared.remove(id);
        prefs.edit().putStringSet(KEY_CLEARED_COMPLAINTS, cleared).apply();
    }
}
