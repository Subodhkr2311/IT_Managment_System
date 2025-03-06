package com.example.it_management_system;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {
    private static final String TAG = "JourneyAdapter";
    private List<Complaints.JourneyEvent> journeyEvents;

    public JourneyAdapter(List<Complaints.JourneyEvent> journeyEvents) {
        this.journeyEvents = journeyEvents != null ? journeyEvents : new ArrayList<>();
        // Sort events by timestamp in descending order (newest first)
        sortJourneyEvents();

        // Log the events for debugging
        for (int i = 0; i < this.journeyEvents.size(); i++) {
            Complaints.JourneyEvent event = this.journeyEvents.get(i);
            Log.d(TAG, "Event " + i + ": " + event.getEvent() + " at " + event.getTimestamp());
        }
    }

    private void sortJourneyEvents() {
        if (journeyEvents.size() > 1) {
            Collections.sort(journeyEvents, new Comparator<Complaints.JourneyEvent>() {
                @Override
                public int compare(Complaints.JourneyEvent e1, Complaints.JourneyEvent e2) {
                    // Sort in reverse order (newest first)
                    return e2.getTimestamp().compareTo(e1.getTimestamp());
                }
            });
        }
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journey_event, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        if (position < journeyEvents.size()) {
            Complaints.JourneyEvent event = journeyEvents.get(position);
            if (event != null) {
                // Set event text
                holder.eventDescription.setText(event.getEvent());
                holder.eventTimestamp.setText(event.getTimestamp());

                // For debugging
                Log.d(TAG, "Binding view at position " + position + " with event: " + event.getEvent());
            } else {
                Log.e(TAG, "Event at position " + position + " is null");
                holder.eventDescription.setText("Unknown event");
                holder.eventTimestamp.setText("");
            }
        } else {
            Log.e(TAG, "Position " + position + " is out of bounds for journeyEvents size " + journeyEvents.size());
        }
    }

    @Override
    public int getItemCount() {
        int count = journeyEvents.size();
        Log.d(TAG, "Item count: " + count);
        return count;
    }

    public void updateEvents(List<Complaints.JourneyEvent> newEvents) {
        this.journeyEvents.clear();
        if (newEvents != null) {
            this.journeyEvents.addAll(newEvents);
            sortJourneyEvents();
        }
        notifyDataSetChanged();
        Log.d(TAG, "Events updated, new count: " + journeyEvents.size());
    }

    public static class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView eventDescription;
        TextView eventTimestamp;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            eventDescription = itemView.findViewById(R.id.txt_event_description);
            eventTimestamp = itemView.findViewById(R.id.txt_event_timestamp);
        }
    }
}