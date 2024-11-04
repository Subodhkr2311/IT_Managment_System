package com.example.it_management_system;
public class NotificationModel {
    private String title;
    private String message;
    private boolean isRead;

    // Default constructor required for calls to DataSnapshot.getValue(NotificationModel.class)
    public NotificationModel() {}

    public NotificationModel(String title, String message, boolean isRead) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}

