package com.example.it_management_system;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JourneyEventAdapterUser extends RecyclerView.Adapter<JourneyEventAdapterUser.JourneyEventViewHolder> {

    private List<Complaints.JourneyEvent> journeyEventList;

    public JourneyEventAdapterUser(List<Complaints.JourneyEvent> journeyEventList) {
        this.journeyEventList = journeyEventList;
        Log.d("JourneyEventAdapterUser", "Adapter created with " +
                (journeyEventList != null ? journeyEventList.size() : 0) + " items");
    }

    @NonNull
    @Override
    public JourneyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journey_event_user, parent, false);
        return new JourneyEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyEventViewHolder holder, int position) {
        Complaints.JourneyEvent journeyEvent = journeyEventList.get(position);
        Log.d("JourneyEventAdapterUser", "Binding: " + journeyEvent.getEvent() + " at " + journeyEvent.getTimestamp());
        holder.event.setText(journeyEvent.getEvent());
        holder.timestamp.setText(journeyEvent.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return journeyEventList != null ? journeyEventList.size() : 0;
    }

    public static class JourneyEventViewHolder extends RecyclerView.ViewHolder {
        TextView event, timestamp;

        public JourneyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            event = itemView.findViewById(R.id.event_textUser);
            timestamp = itemView.findViewById(R.id.timestamp_textUser);
        }
    }
}