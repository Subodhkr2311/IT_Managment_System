package com.example.it_management_system;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintsAdapterExecutive extends RecyclerView.Adapter<ComplaintsAdapterExecutive.ComplaintsViewHolder> {

    private List<Complaints> complaintsList;
    private Context context;
    private tabComplaintsExecutive fragment;

    public ComplaintsAdapterExecutive(List<Complaints> complaintsList, Context context, tabComplaintsExecutive fragment) {
        this.complaintsList = complaintsList;
        this.context = context;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ComplaintsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_executivecomplaint, parent, false);
        return new ComplaintsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintsViewHolder holder, int position) {
        Complaints complaint = complaintsList.get(position);

        holder.title.setText(complaint.getTitle());
        holder.date.setText(complaint.getDate());
        holder.location.setText(complaint.getLocation());

        holder.itemView.setOnLongClickListener(v -> {
            showJourneyBottomSheet(complaintsList.get(position));
            return true;
        });

        holder.status.setText(complaint.getStatus());
        if ("Claimed".equals(complaint.getStatus())) {
            holder.status.setTextColor(Color.GREEN); // Example: green for claimed tickets
        } else {
            holder.status.setTextColor(Color.RED); // Example: red for other statuses
        }

        // Show description only if available
        if (complaint.getDescription() != null && !complaint.getDescription().isEmpty()) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(complaint.getDescription());
        } else {
            holder.description.setVisibility(View.GONE);
        }

        // Click listener for the claim action
        holder.itemView.setOnClickListener(v -> fragment.claimTicket(complaint));
    }

    @Override
    public int getItemCount() {
        return complaintsList.size();
    }

    public static class ComplaintsViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, location, status, description;

        public ComplaintsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            location = itemView.findViewById(R.id.tvLocation);
            status = itemView.findViewById(R.id.tvStatus);
            description = itemView.findViewById(R.id.tvDescription);
        }
    }

    private void showJourneyBottomSheet(Complaints complaint) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;

            // For executive view
            TicketJourneyBottomSheet bottomSheet =
                    TicketJourneyBottomSheet.newInstance(complaint, TicketJourneyBottomSheet.ROLE_EXECUTIVE);

            bottomSheet.show(activity.getSupportFragmentManager(), "TicketJourney");
        }
    }
}