package com.example.it_management_system;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Complaints implements Serializable{
    private String translatedTitle;
    private String translatedDescription;
    // Use long for timestamp to represent milliseconds since epoch
    private String photoUrl;
    private String id;
    private String userId;
    @PropertyName("complaintDescription")
    private String description;
    @PropertyName("complaintStatus")
    private String status;
    @PropertyName("complaintTitle")
    private String title;
    @PropertyName("complaintType")
    private String complaintType;
    @PropertyName("dateTime")
    private String date;
    @PropertyName("location")
    private String location;

    private String assignedTo;
    private String userEmail;
    // Changed from List to Map to match Firebase's storage format.
    private Map<String, JourneyEvent> ticketJourney;
    private String resolvedBy; // Executive who resolved the ticket

    // New fields for resolution process
    private String resolutionStatus; // e.g., Arriving, Resolved, Not Resolved, Pending
    private String resolutionReason; // Reason if Not Resolved
    private String userFeedback;
    private int userRating;
    // Getters and Setters for resolvedBy
    public String getResolvedBy() {
        return resolvedBy;
    }
    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Complaints() {
        this.status = "New";
        this.resolutionStatus = "Pending";
        this.ticketJourney = new HashMap<>();
    }
    public Complaints(String complaintTitle, String complaintStatus, String location, long timestamp) {
        this.title = complaintTitle;
        this.status = complaintStatus;
        this.location = location;
        this.ticketJourney = new HashMap<>();
    }

    public Complaints(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = "New";
        this.assignedTo = null;
        this.ticketJourney = new HashMap<>();
    }

    public Complaints(String userId, String location, String complaintType, String title, String description, String status, long timestamp, String photoUrl, String date) {
        this.userId = userId;
        this.location = location;
        this.complaintType = complaintType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.photoUrl = photoUrl;
        this.date = date;
        this.ticketJourney = new HashMap<>();
    }
    public String getResolutionStatus() {
        return resolutionStatus;
    }
    public void setResolutionStatus(String resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }
    public String getResolutionReason() {
        return resolutionReason;
    }
    public void setResolutionReason(String resolutionReason) {
        this.resolutionReason = resolutionReason;
    }
    public String getUserFeedback() {
        return userFeedback;
    }
    public void setUserFeedback(String userFeedback) {
        this.userFeedback = userFeedback;
    }
    public int getUserRating() {
        return userRating;
    }
    public void setUserRating(int userRating) {
        this.userRating = userRating;
    }


    // Other getters and setters ...
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getuserEmail() { return userEmail; }
    public void setuserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    // For Firebase deserialization, ticketJourney is stored as a Map.
    public Map<String, JourneyEvent> getTicketJourneyMap() {
        if (ticketJourney == null) {
            ticketJourney = new HashMap<>();
        }
        return ticketJourney;
    }
    public void setTicketJourney(Map<String, JourneyEvent> ticketJourney) {
        this.ticketJourney = ticketJourney != null ? ticketJourney : new HashMap<>();
    }

    @Exclude
    public List<JourneyEvent> getTicketJourney() {
        if (ticketJourney == null) {
            Log.d("complaints", "ticketJourney is null, returning empty list");
            return new ArrayList<>();
        }
        Log.d("complaints", "ticketJourney has " + ticketJourney.size() + " entries");
        // Sort events by timestamp if needed
        List<JourneyEvent> events = new ArrayList<>(ticketJourney.values());
        return events;
    }

    // Add a new journey event with better error handling
    public void addJourneyEvent(String event, String timestamp) {
        if (this.ticketJourney == null) {
            this.ticketJourney = new HashMap<>();
            Log.d("complaints", "Created new ticketJourney map");
        }

        try {
            String key = String.valueOf(System.currentTimeMillis());
            JourneyEvent journeyEvent = new JourneyEvent(event, timestamp);
            this.ticketJourney.put(key, journeyEvent);
            Log.d("complaints", "Added new journey event: " + event + " at " + timestamp + " with key " + key);
        } catch (Exception e) {
            Log.e("complaints", "Error adding journey event: " + e.getMessage(), e);
        }
    }
    public String getTranslatedTitle() {
        return translatedTitle != null ? translatedTitle : getTitle();
    }
    public void setTranslatedTitle(String translatedTitle) { this.translatedTitle = translatedTitle; }
    public String getTranslatedDescription() {
        return translatedDescription != null ? translatedDescription : getDescription();
    }
    public void setTranslatedDescription(String translatedDescription) { this.translatedDescription = translatedDescription; }

    public static class JourneyEvent implements Serializable {
        public String event;
        public String timestamp;
        public JourneyEvent() {} // Empty constructor for Firebase
        public JourneyEvent(String event, String timestamp) {
            this.event = event;
            this.timestamp = timestamp;
        }
        public String getEvent() { return event; }
        public String getTimestamp() { return timestamp; }
    }
    // Helper method to convert timestamp to date string
    private String convertTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultDate = new Date(timestamp);
        return sdf.format(resultDate);
    }
}
