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

    public interface OnExecutiveClickListener {
        void onExecutiveClick(User executive);
    }

    private List<User> executivesList;
    // Keep a copy of the original list for filtering
    private List<User> originalList;
    private OnExecutiveClickListener listener;

    public ExecutivesAdapter(List<User> executivesList, OnExecutiveClickListener listener) {
        this.executivesList = executivesList;
        // Create a copy of the list for filtering
        this.originalList = new ArrayList<>(executivesList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExecutiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_executive, parent, false);
        return new ExecutiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExecutiveViewHolder holder, int position) {
        User executive = executivesList.get(position);
        holder.tvName.setText(executive.getName());
        holder.tvEmail.setText(executive.getEmail());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExecutiveClick(executive);
            }
        });
    }

    @Override
    public int getItemCount() {
        return executivesList.size();
    }

    // Update both the original and filtered lists
    public void updateList(List<User> newList) {
        this.originalList = new ArrayList<>(newList);
        this.executivesList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // Filter the list by search query (name and email only)
    public void filter(String searchQuery, String availability) {
        List<User> filteredList = new ArrayList<>();
        for (User user : originalList) {
            boolean matchesQuery = user.getName().toLowerCase().contains(searchQuery) ||
                    user.getEmail().toLowerCase().contains(searchQuery);
            if (matchesQuery) {
                filteredList.add(user);
            }
        }
        this.executivesList = filteredList;
        notifyDataSetChanged();
    }

    static class ExecutiveViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        public ExecutiveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExecutiveNameItem);
            tvEmail = itemView.findViewById(R.id.tvExecutiveEmailItem);
        }
    }
}
