package com.example.it_management_system;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private String email;
    private String role;
    private String password; // Add password (hashed)
    private Map<String, Boolean> complaintIds;
    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private boolean isAvailable;

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }// Change to Map<String, Boolean>

    // Default constructor required for Firebase
    public User() {
        this.complaintIds = new HashMap<>(); // Initialize the complaintIds as a HashMap
    }

    // Constructor with parameters
    public User(String name, String email, String role, String password) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = hashPassword(password); // Hash the password before setting it
        this.complaintIds = new HashMap<>(); // Initialize the complaintIds as a HashMap
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = hashPassword(password); // Hash the password before setting it
    }

    // Method to hash the password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Boolean> getComplaintIds() {
        return complaintIds;
    }

    public void setComplaintIds(Map<String, Boolean> complaintIds) {
        this.complaintIds = complaintIds;
    }

    // Method to add a complaint ID
    public void addComplaintId(String complaintId) {
        if (!complaintIds.containsKey(complaintId)) { // Avoid duplicates
            complaintIds.put(complaintId, true); // Add the complaint ID to the map with a value of true
        }
    }
}