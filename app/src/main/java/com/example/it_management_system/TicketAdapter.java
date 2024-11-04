package com.example.it_management_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Complaints> complaintsList;
    private List<Complaints> filteredList;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Complaints complaint);
    }

    public TicketAdapter(List<Complaints> complaintsList, OnTicketClickListener listener) {
        this.complaintsList = complaintsList != null ? complaintsList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.complaintsList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        if (position >= 0 && position < filteredList.size()) {
            Complaints complaint = filteredList.get(position);
            if (complaint != null) {
                holder.bind(complaint);
            }
        }
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }
    public void updateList(List<Complaints> newList) {
        complaintsList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        filteredList = new ArrayList<>(complaintsList);
        notifyDataSetChanged();
    }

    public void filter(String query, String filter) {
        if (query == null) query = "";
        if (filter == null) filter = "All";

        filteredList = new ArrayList<>();

        for (Complaints complaint : complaintsList) {
            if (complaint == null) continue;

            boolean matchesSearch = query.isEmpty() ||
                    (complaint.getTitle() != null && complaint.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                    (complaint.getuserEmail() != null && complaint.getuserEmail().toLowerCase().contains(query.toLowerCase())) ||
                    (complaint.getDate() != null && complaint.getDate().toLowerCase().contains(query.toLowerCase())) ||
                    (complaint.getLocation() != null && complaint.getLocation().toLowerCase().contains(query.toLowerCase()));

            boolean matchesFilter = filter.equals("All") ||
                    (filter.equals("Pending") && "Pending".equals(complaint.getStatus())) ||
                    (filter.equals("Resolved") && "Resolved".equals(complaint.getStatus())) ||
                    (filter.equals("Assigned") && complaint.getAssignedTo() != null && !complaint.getAssignedTo().isEmpty()) ||
                    (filter.equals("Unassigned") && (complaint.getAssignedTo() == null || complaint.getAssignedTo().isEmpty()));

            if (matchesSearch && matchesFilter) {
                filteredList.add(complaint);
            }
        }
        notifyDataSetChanged();
    }
    // ... (keep existing updateList and filter methods)

    class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, emailTextView, dateTimeTextView, locationTextView;
        Chip statusChip;
        TextView assignedToTextView;
        Context context;

        TicketViewHolder(View itemView) {
            super(itemView);
            // Initialize context from itemView
            context = itemView.getContext();

            titleTextView = itemView.findViewById(R.id.ticket_title);
            emailTextView = itemView.findViewById(R.id.ticket_user_email);
            dateTimeTextView = itemView.findViewById(R.id.ticket_date_time);
            locationTextView = itemView.findViewById(R.id.ticket_location);
            statusChip = itemView.findViewById(R.id.ticket_status);
            assignedToTextView = itemView.findViewById(R.id.ticket_assigned_to);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < filteredList.size()) {
                    Complaints complaint = filteredList.get(position);
                    if ("Resolved".equals(complaint.getStatus())) {
                        showCannotAssignDialog();
                    } else {
                        listener.onTicketClick(complaint);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < filteredList.size()) {
                    showJourneyDialog(filteredList.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(Complaints complaint) {
            if (complaint == null) return;

            titleTextView.setText(complaint.getTitle() != null ? complaint.getTitle() : "");
            emailTextView.setText(complaint.getuserEmail() != null ? complaint.getuserEmail() : "");
            dateTimeTextView.setText(complaint.getDate() != null ? complaint.getDate() : "");
            locationTextView.setText(complaint.getLocation() != null ? complaint.getLocation() : "");
            statusChip.setText(complaint.getStatus() != null ? complaint.getStatus() : "");

            if (complaint.getAssignedTo() != null && !complaint.getAssignedTo().isEmpty()) {
                assignedToTextView.setVisibility(View.VISIBLE);
                assignedToTextView.setText("Assigned to: " + complaint.getAssignedTo());
            } else {
                assignedToTextView.setVisibility(View.GONE);
            }
        }

        private void showJourneyDialog(Complaints complaint) {
            // Create custom dialog layout
            View dialogView = LayoutInflater.from(context).inflate(R.layout.journey_recycler_view_admin, null);
            RecyclerView journeyRecyclerView = dialogView.findViewById(R.id.journey_recycler_view_admin);
            TextView titleView = dialogView.findViewById(R.id.journey_title_admin);

            // Set up RecyclerView
            journeyRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            List<Complaints.JourneyEvent> journeyEvents = complaint.getTicketJourney();
            JourneyAdapterAdmin journeyAdapter = new JourneyAdapterAdmin(journeyEvents);
            journeyRecyclerView.setAdapter(journeyAdapter);

            // Create and show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Ticket Journey")
                    .setView(dialogView)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        private void showCannotAssignDialog() {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Cannot Assign")
                    .setMessage("This ticket has already been resolved and cannot be assigned.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .setBackground(context.getResources().getDrawable(R.drawable.edit_text_background))
                    .show();
        }
    }
}