package com.example.it_management_system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JourneyAdapterAdmin extends RecyclerView.Adapter<JourneyAdapterAdmin.JourneyViewHolder> {
    private List<Complaints.JourneyEvent> journeyEvents;

    public JourneyAdapterAdmin(List<Complaints.JourneyEvent> journeyEvents) {
        this.journeyEvents = journeyEvents;
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey_event_admin, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        holder.bind(journeyEvents.get(position));
    }

    @Override
    public int getItemCount() {
        return journeyEvents != null ? journeyEvents.size() : 0;
    }

    class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView eventTextView, timestampTextView;

        JourneyViewHolder(View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.event_textAdmin);
            timestampTextView = itemView.findViewById(R.id.timestamp_textAdmin);
        }

        void bind(Complaints.JourneyEvent journeyEvent) {
            if (journeyEvent != null) {
                eventTextView.setText(journeyEvent.getEvent());
                timestampTextView.setText(journeyEvent.getTimestamp());
            }
        }
    }
}
