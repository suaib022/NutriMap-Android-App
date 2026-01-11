package com.example.nutrimap.domain.model;

public class Visit {
    private int id;
    private String documentId; // Firestore document ID
    private int childId; // Legacy integer ID for backward compatibility
    private String childDocumentId; // Firestore document ID of parent child
    private String visitDate; // Format: yyyy-MM-dd
    private double weightKg;
    private double heightCm;
    private int muacMm;
    private String notes;

    private String riskLevel = "N/A";
    private String createdAt;
    private String updatedAt;
    private Integer enteredBy;
    private boolean deleted = false;

    public Visit() {}

    public Visit(int id, int childId, String visitDate, double weightKg, double heightCm, int muacMm, String notes,
                 String riskLevel, String createdAt, String updatedAt, Integer enteredBy, boolean deleted) {
        this.id = id;
        this.childId = childId;
        this.visitDate = visitDate;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.muacMm = muacMm;
        this.notes = notes;
        this.riskLevel = riskLevel != null ? riskLevel : "N/A";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.enteredBy = enteredBy;
        this.deleted = deleted;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public int getChildId() { return childId; }
    public void setChildId(int childId) { this.childId = childId; }

    public String getChildDocumentId() { return childDocumentId; }
    public void setChildDocumentId(String childDocumentId) { this.childDocumentId = childDocumentId; }

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

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Integer getEnteredBy() { return enteredBy; }
    public void setEnteredBy(Integer enteredBy) { this.enteredBy = enteredBy; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
