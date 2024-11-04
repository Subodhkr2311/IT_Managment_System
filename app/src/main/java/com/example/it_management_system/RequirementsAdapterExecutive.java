package com.example.it_management_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RequirementsAdapterExecutive extends RecyclerView.Adapter<RequirementsAdapterExecutive.RequirementViewHolder> {

    private List<RequirementModel> requirementList;
    private Context context;
    private DatabaseReference usersRef;

    public RequirementsAdapterExecutive(List<RequirementModel> requirementList, Context context) {
        this.requirementList = requirementList;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @Override
    public RequirementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requirement_executive, parent, false);
        return new RequirementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RequirementViewHolder holder, int position) {
        RequirementModel requirement = requirementList.get(position);

        holder.itemTextView.setText("Required item : "+requirement.getItem() +"\nItem count: "+ requirement.getItemCount());
        holder.locationTextView.setText("Location: "+requirement.getLocation());
        holder.dateTextView.setText("From: "+requirement.getFromDate());
        holder.timeTextView.setText("To: "+requirement.getToDate());

       holder.demandedByTextView.setText("Demanded by: "+ requirement.getDemandedBy());

        holder.itemView.setEnabled(true);
        holder.itemView.setAlpha(1.0f);
        holder.itemView.setOnClickListener(v -> showClaimDialog(requirement, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return requirementList.size();
    }

    public class RequirementViewHolder extends RecyclerView.ViewHolder {
        TextView itemTextView, locationTextView, dateTextView, timeTextView, demandedByTextView;

        public RequirementViewHolder(View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.itemTextViewExecutive);
            locationTextView = itemView.findViewById(R.id.locationTextViewExecutive);
            dateTextView = itemView.findViewById(R.id.dateTextViewExecutive);
            timeTextView = itemView.findViewById(R.id.timeTextViewExecutive);
            demandedByTextView = itemView.findViewById(R.id.demandedByTextViewExecutive);
        }
    }

    // Function to show claim dialog
    private void showClaimDialog(RequirementModel requirement, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Claim Requirement")
                .setMessage("Are you sure you want to claim this requirement?")
                .setPositiveButton("Claim", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = auth.getCurrentUser();

                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                        userRef.child("name").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String executiveName = task.getResult().getValue(String.class);

                                if (executiveName != null && !executiveName.isEmpty()) {
                                    userRef.child("role").get().addOnCompleteListener(roleTask -> {
                                        if (roleTask.isSuccessful()) {
                                            String role = roleTask.getResult().getValue(String.class);
                                            if ("executive".equals(role)) {
                                                DatabaseReference requirementsRef = FirebaseDatabase.getInstance().getReference("requirements").child(requirement.getId());

                                                requirementsRef.child("assignedTo").setValue(executiveName)
                                                        .addOnSuccessListener(aVoid -> {
                                                            userRef.child("assignedRequirements").child(requirement.getId()).setValue(true)
                                                                    .addOnSuccessListener(aVoid1 -> {
                                                                        Toast.makeText(context, "Requirement claimed and assigned to " + executiveName, Toast.LENGTH_SHORT).show();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(context, "Failed to assign to user", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Failed to claim requirement", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Toast.makeText(context, "User is not an executive", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to check user role", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "Name not found for the executive", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}