package com.example.it_management_system;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {
    private List<Complaints> complaintList;

    public ComplaintAdapter(List<Complaints> complaintList) {
        this.complaintList = complaintList;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.complaint_item, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaints complaint = complaintList.get(position);
        Log.d("ComplaintAdapter", "Binding complaint: " + complaint.getTitle());

        // Use translated title if available, otherwise use original title
        String titleText = complaint.getTranslatedTitle() != null ?
                complaint.getTranslatedTitle() :
                (complaint.getTitle() != null ? complaint.getTitle() : "No Title");
        holder.title.setText(titleText);

        // Use translated description if available, otherwise use original description
        String descriptionText = complaint.getTranslatedDescription() != null ?
                complaint.getTranslatedDescription() :
                complaint.getDescription();

        if (descriptionText == null || descriptionText.isEmpty()) {
            holder.description.setVisibility(View.GONE); // Hide if no description
        } else {
            holder.description.setText(descriptionText);
            holder.description.setVisibility(View.VISIBLE); // Show if description exists
        }

        holder.status.setText(complaint.getStatus() != null ? complaint.getStatus() : "Status Unknown");
        holder.date.setText(complaint.getDate() != null ? complaint.getDate() : "No Date");
        holder.location.setText(complaint.getLocation() != null ? complaint.getLocation() : "No Location");

        // Show journey dialog on item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJourneyDialog(holder.itemView.getContext(), complaint.getTicketJourney());
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaintList != null ? complaintList.size() : 0;
    }

    public void updateList(List<Complaints> newList) {
        this.complaintList = newList;
        notifyDataSetChanged();
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

    private void showJourneyDialog(Context context, List<Complaints.JourneyEvent> journeyEvents) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_ticket_journey_user);

        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_journey_events_user);
        if (recyclerView != null) {
            JourneyEventAdapterUser adapter = new JourneyEventAdapterUser(journeyEvents);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            Log.e("JourneyDialog", "RecyclerView is null. Please check your layout and ID reference.");
        }

        Button closeButton = dialog.findViewById(R.id.close_button_user);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        } else {
            Log.e("JourneyDialog", "Close button is null. Please check your layout and ID reference.");
        }

        dialog.show();
    }
}