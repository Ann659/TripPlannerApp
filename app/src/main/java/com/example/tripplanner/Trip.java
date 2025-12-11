package com.example.tripplanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Trip {
    private String id;
    private String tripName;
    private double tripBudget;
    private Date startDate;

    public Trip() {}

    public Trip(String tripName, double tripBudget, Date startDate) {
        this.id = UUID.randomUUID().toString();
        this.tripName = tripName;
        this.tripBudget = tripBudget;
        this.startDate = startDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTripName() { return tripName; }
    public void setTripName(String tripName) { this.tripName = tripName; }

    public double getTripBudget() { return tripBudget; }
    public void setTripBudget(double tripBudget) { this.tripBudget = tripBudget; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public String getFormattedDate() {
        if (startDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(startDate);
        }
        return "";
    }

    public String getFormattedBudget() {
        return String.format(Locale.getDefault(), "%.2f ₽", tripBudget);
    }
}