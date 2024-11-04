package com.example.it_management_system;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class Complaints {

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
    private List<JourneyEvent> ticketJourney;
    private String resolvedBy; // Add this field

    // Add getter and setter
    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }



    public Complaints() {
        this.status = "New";
        this.ticketJourney = new ArrayList<>(); // Set default status
    }

    public Complaints(String complaintTitle, String complaintStatus, String location, long timestamp) {
        this.title = complaintTitle;
        this.status = complaintStatus;
        this.location = location;

    }
    public Complaints(String id, String title, String description) {
        this.id = id;
        this.title = title ;
        this.description = description;
        this.status = "New";
        this.assignedTo = null;
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
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getuserEmail() {
        return userEmail;
    }

    public void setuserEmail(String userId) {
        this.userEmail = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setJourneyEvents(List<JourneyEvent> journeyEvents) {
        this.ticketJourney = journeyEvents;
    }

    public void addJourneyEvent(String event, String timestamp) {
        if (this.ticketJourney == null) {
            this.ticketJourney = new ArrayList<>();
        }
        this.ticketJourney.add(new JourneyEvent(event, timestamp));
    }

    public List<JourneyEvent> getTicketJourney() {
        return ticketJourney;
    }

    public static class JourneyEvent {
        public String event;
        public String timestamp;

        public JourneyEvent() {} // Empty constructor for Firebasea

        public JourneyEvent(String event, String timestamp) {
            this.event = event;
            this.timestamp = timestamp;
        }
        public String getEvent() {
            return event;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
    // Helper method to convert timestamp to date string
    private String convertTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date resultDate = new Date(timestamp);
        return sdf.format(resultDate);
    }

}


