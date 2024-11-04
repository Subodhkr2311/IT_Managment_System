package com.example.it_management_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequirementRecyclerAdapterAdmin extends RecyclerView.Adapter<RequirementRecyclerAdapterAdmin.ViewHolder> {

    private Context context;
    private List<RequirementModel> requirementsList;

    public RequirementRecyclerAdapterAdmin(Context context, List<RequirementModel> requirementsList) {
        this.context = context;
        this.requirementsList = requirementsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_requirement_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequirementModel requirement = requirementsList.get(position);

        // Set item name and details in the custom layout
        holder.itemNameTextView.setText(requirement.getItem());
        holder.itemDetailsTextView.setText(
                "Count: " + requirement.getItemCount() +
                        "\nLocation: " + requirement.getLocation() +
                        "\nFrom: " + requirement.getFromDate() +
                        "\nTo: " + requirement.getToDate()
        );
    }

    @Override
    public int getItemCount() {
        return requirementsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView, itemDetailsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemDetailsTextView = itemView.findViewById(R.id.itemDetailsTextView);
        }
    }
}
