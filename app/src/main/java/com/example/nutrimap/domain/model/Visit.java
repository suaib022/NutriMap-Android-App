package com.example.nutrimap.domain.model;

public class Visit {
    private int id;
    private int childId;
    private String visitDate; // Format: yyyy-MM-dd
    private double weightKg;
    private double heightCm;
    private int muacMm;
    private String notes;

    public Visit() {}

    public Visit(int id, int childId, String visitDate, double weightKg, double heightCm, int muacMm, String notes) {
        this.id = id;
        this.childId = childId;
        this.visitDate = visitDate;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.muacMm = muacMm;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChildId() { return childId; }
    public void setChildId(int childId) { this.childId = childId; }

    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public int getMuacMm() { return muacMm; }
    public void setMuacMm(int muacMm) { this.muacMm = muacMm; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
