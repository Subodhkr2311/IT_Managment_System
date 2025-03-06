package com.example.it_management_system;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {
    private static final String TAG = "ComplaintAdapter";

    private List<Complaints> complaintList;
    private List<Complaints> selectedComplaints = new ArrayList<>();
    private Context context;

    public ComplaintAdapter(List<Complaints> complaintList) {
        this.complaintList = complaintList != null ? complaintList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.complaint_item, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaints complaint = complaintList.get(position);
        Log.d(TAG, "Binding complaint: " + complaint.getTitle());

        String titleText = complaint.getTranslatedTitle() != null ?
                complaint.getTranslatedTitle() :
                (complaint.getTitle() != null ? complaint.getTitle() : "No Title");
        holder.title.setText(titleText);

        String descriptionText = complaint.getTranslatedDescription() != null ?
                complaint.getTranslatedDescription() :
                complaint.getDescription();
        if (descriptionText == null || descriptionText.isEmpty()) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setText(descriptionText);
            holder.description.setVisibility(View.VISIBLE);
        }

        holder.status.setText(complaint.getStatus() != null ? complaint.getStatus() : "Status Unknown");
        holder.date.setText(complaint.getDate() != null ? complaint.getDate() : "No Date");
        holder.location.setText(complaint.getLocation() != null ? complaint.getLocation() : "No Location");

        // Toggle selection on item click.
        holder.itemView.setOnClickListener(v -> {
            if (selectedComplaints.contains(complaint)) {
                selectedComplaints.remove(complaint);
                holder.itemView.setAlpha(1.0f);
            } else {
                selectedComplaints.add(complaint);
                holder.itemView.setAlpha(0.5f);
            }
        });

        // Open journey bottom sheet on long click.
        holder.itemView.setOnLongClickListener(v -> {
            try {
                showJourneyBottomSheet(complaint);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error showing journey bottom sheet", e);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaintList != null ? complaintList.size() : 0;
    }

    public void updateList(List<Complaints> newList) {
        this.complaintList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        selectedComplaints.clear();
        notifyDataSetChanged();
    }

    public List<Complaints> getSelectedComplaints() {
        return selectedComplaints;
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, status, date, location;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.complaint_TitleS);
            description = itemView.findViewById(R.id.complaint_DescriptionS);
            status = itemView.findViewById(R.id.complaint_StatuS);
            date = itemView.findViewById(R.id.complaint_DateS);
            location = itemView.findViewById(R.id.complaint_location);
        }
    }

    private void showJourneyBottomSheet(Complaints complaint) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;

            // We assume the user role is "User" here
            // In a real app, you'd get this from your auth/session system
            TicketJourneyBottomSheet bottomSheet =
                    TicketJourneyBottomSheet.newInstance(complaint, TicketJourneyBottomSheet.ROLE_USER);

            bottomSheet.show(activity.getSupportFragmentManager(), "TicketJourney");
        } else {
            Log.e(TAG, "Context is not an AppCompatActivity");
        }
    }
}