package com.example.it_management_system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<RequirementModel> requirementList;

    public HistoryAdapter(List<RequirementModel> requirementList) {
        this.requirementList = requirementList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requirement, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        RequirementModel requirement = requirementList.get(position);

        holder.textViewItem.setText(requirement.getItem()+ "\nItem count: "+requirement.getItemCount());
        holder.textViewLocation.setText("At: "+requirement.getLocation());
        holder.From.setText("From: " + requirement. getFromDate());
        holder.To.setText("To: " + requirement.getToDate());
    }

    @Override
    public int getItemCount() {
        return requirementList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView textViewItem, textViewLocation, From, To;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.textViewItem);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            From= itemView.findViewById(R.id.textViewDate);
           To = itemView.findViewById(R.id.textViewTime);
        }
    }
}
