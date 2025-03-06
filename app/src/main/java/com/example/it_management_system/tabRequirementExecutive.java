package com.example.it_management_system;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class tabRequirementExecutive extends Fragment {

    private RecyclerView recyclerViewRequirements;
    private RequirementsAdapterExecutive adapter;
    private DatabaseReference databaseReference;
    private List<RequirementModel> requirementList = new ArrayList<>();
    private int currentFilter = MyticketsExecutiveFragment.FILTER_ALL;
    public tabRequirementExecutive() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_requirement_executive, container, false);

        recyclerViewRequirements = view.findViewById(R.id.recyclerViewRequirementsExecutive);
        recyclerViewRequirements.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("requirements");

        // Fetch requirements data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                requirementList.clear(); // Clear list before adding new data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RequirementModel requirement = snapshot.getValue(RequirementModel.class);
                    if (requirement != null) {
                        requirementList.add(requirement); // Add each requirement to the list
                    }
                }applyFilter(currentFilter);
                Collections.sort(requirementList, (req1, req2) -> {
                    if (req1.getFromDate() == null && req2.getFromDate() == null) return 0;
                    if (req1.getFromDate() == null) return 1;
                    if (req2.getFromDate() == null) return -1;
                    return req2.getFromDate().compareTo(req1.getFromDate());
                });

                // Set the adapter to the RecyclerView
                adapter = new RequirementsAdapterExecutive(requirementList, getContext());
                recyclerViewRequirements.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load requirements", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    public void applyFilter(int filterType) {
        currentFilter = filterType;
        List<RequirementModel> filteredList = new ArrayList<>();

        for (RequirementModel requirement : requirementList) {
            if (MyticketsExecutiveFragment.matchesFilter(requirement.getAssignedTo(),
                    requirement.getFromDate(), filterType)) {
                filteredList.add(requirement);
            }
        }}
}
