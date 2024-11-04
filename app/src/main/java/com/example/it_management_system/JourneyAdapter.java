// JourneyAdapter.java
package com.example.it_management_system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private List<Complaints.JourneyEvent> journeyEvents;

    public JourneyAdapter(List<Complaints.JourneyEvent> journeyEvents) {
        this.journeyEvents = journeyEvents;
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey_event, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        Complaints.JourneyEvent journeyEvent = journeyEvents.get(position);
        holder.tvEvent.setText(journeyEvent.event);
        holder.tvTimestamp.setText(journeyEvent.timestamp);
    }

    @Override
    public int getItemCount() {
        return journeyEvents.size();
    }

    static class JourneyViewHolder extends RecyclerView.ViewHolder {

        TextView tvEvent, tvTimestamp;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEvent = itemView.findViewById(R.id.tvEvent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}