package com.example.it_management_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Complaints> complaintsList;
    private List<Complaints> filteredList;
    private OnTicketClickListener listener;

    // Fields for multi‑select support
    private boolean selectionMode = false;
    private List<Complaints> selectedComplaints = new ArrayList<>();
    private OnSelectionChangedListener selectionListener;

    public interface OnTicketClickListener {
        // Callback for normal item click (when not in selection mode)
        void onTicketClick(Complaints complaint);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public TicketAdapter(List<Complaints> complaintsList, OnTicketClickListener listener) {
        this.complaintsList = (complaintsList != null) ? complaintsList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.complaintsList);
        this.listener = listener;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    // Methods to control selection mode
    public void enableSelectionMode() {
        selectionMode = true;
        selectedComplaints.clear();
        notifyDataSetChanged();
    }

    public void disableSelectionMode() {
        selectionMode = false;
        selectedComplaints.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public List<Complaints> getSelectedComplaints() {
        return selectedComplaints;
    }

    public void toggleSelection(Complaints complaint) {
        if (selectedComplaints.contains(complaint)) {
            selectedComplaints.remove(complaint);
        } else {
            selectedComplaints.add(complaint);
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedComplaints.size());
        }
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
                // Show selection indicator when in selection mode
                if (selectionMode) {
                    holder.selectionIndicator.setVisibility(selectedComplaints.contains(complaint) ? View.VISIBLE : View.GONE);
                } else {
                    holder.selectionIndicator.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (filteredList != null) ? filteredList.size() : 0;
    }

    public void updateList(List<Complaints> newList) {
        complaintsList = (newList != null) ? new ArrayList<>(newList) : new ArrayList<>();
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

    class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, emailTextView, dateTimeTextView, locationTextView;
        Chip statusChip;
        TextView assignedToTextView;
        ImageView selectionIndicator;
        Context context;

        TicketViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            titleTextView = itemView.findViewById(R.id.ticket_title);
            emailTextView = itemView.findViewById(R.id.ticket_user_email);
            dateTimeTextView = itemView.findViewById(R.id.ticket_date_time);
            locationTextView = itemView.findViewById(R.id.ticket_location);
            statusChip = itemView.findViewById(R.id.ticket_status);
            assignedToTextView = itemView.findViewById(R.id.ticket_assigned_to);
            selectionIndicator = itemView.findViewById(R.id.selection_indicator);

            // Normal click
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < filteredList.size()) {
                    Complaints complaint = filteredList.get(position);
                    if (selectionMode) {
                        TicketAdapter.this.toggleSelection(complaint);
                    } else {
                        listener.onTicketClick(complaint);
                    }
                }
            });

            // Long press starts selection mode
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < filteredList.size()) {
                    if (!selectionMode) {
                        TicketAdapter.this.enableSelectionMode();
                    }
                    TicketAdapter.this.toggleSelection(filteredList.get(position));
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
    }
}
