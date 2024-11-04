package com.example.it_management_system;

public class RequirementModel {
    private String item;
    private String location;
    private String fromDate;
    private String toDate;
    private String itemCount;
    private String demandedBy;    // Make sure this matches exactly
    private String id;
    private String assignedTo;

    // Empty constructor required for Firebase
    public RequirementModel() {
    }

    public RequirementModel(String item, String location, String fromDate, String toDate,
                            String itemCount, String demandedBy, String id) {
        this.item = item;
        this.location = location;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.itemCount = itemCount;
        this.demandedBy = demandedBy;   // Set the demandedBy value
        this.id = id;
    }

    // Getters and setters
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }

    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }

    public String getItemCount() { return itemCount; }
    public void setItemCount(String itemCount) { this.itemCount = itemCount; }

    public String getDemandedBy() { return demandedBy; }
    public void setDemandedBy(String demandedBy) { this.demandedBy = demandedBy; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}