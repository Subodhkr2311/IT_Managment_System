package com.example.it_management_system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExecutivesAdapter extends RecyclerView.Adapter<ExecutivesAdapter.ExecutiveViewHolder> {

    private List<User> executivesList;
    private List<User> filteredList;

    public ExecutivesAdapter(List<User> executivesList) {
        this.executivesList = executivesList;
        this.filteredList = new ArrayList<>(executivesList);
    }

    @NonNull
    @Override
    public ExecutiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_executive, parent, false);
        return new ExecutiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExecutiveViewHolder holder, int position) {
        User executive = filteredList.get(position);
        holder.nameTextView.setText(executive.getName());
        holder.emailTextView.setText(executive.getEmail());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateList(List<User> newList) {
        executivesList = new ArrayList<>(newList);
        filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query, String availability) {
        filteredList.clear();
        for (User executive : executivesList) {
            boolean matchesSearch = query.isEmpty() ||
                    executive.getName().toLowerCase().contains(query.toLowerCase()) ||
                    executive.getEmail().toLowerCase().contains(query.toLowerCase());

            boolean matchesAvailability = availability.equals("All") ||
                    (availability.equals("Available") && executive.isAvailable()) ||
                    (availability.equals("Unavailable") && !executive.isAvailable());

            if (matchesSearch && matchesAvailability) {
                filteredList.add(executive);
            }
        }
        notifyDataSetChanged();
    }

    static class ExecutiveViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;

        ExecutiveViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.executive_name);
            emailTextView = itemView.findViewById(R.id.executive_email);
        }
    }
}