package com.example.nutrimap.domain.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Child {
    private int id;
    private String documentId; // Firestore document ID
    private String name;
    private String fatherName;
    private String motherName;
    private String contact;
    private String gender;
    private String dateOfBirth; // Format: yyyy-MM-dd
    private String divisionId;
    private String districtId;
    private String upazilaId;
    private String unionId;
    private String branchId;

    public Child() {}

    public Child(int id, String name, String fatherName, String motherName, String contact,
                 String gender, String dateOfBirth, String divisionId, String districtId,
                 String upazilaId, String unionId, String branchId) {
        this.id = id;
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.contact = contact;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.divisionId = divisionId;
        this.districtId = districtId;
        this.upazilaId = upazilaId;
        this.unionId = unionId;
        this.branchId = branchId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getDivisionId() { return divisionId; }
    public void setDivisionId(String divisionId) { this.divisionId = divisionId; }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    public String getUpazilaId() { return upazilaId; }
    public void setUpazilaId(String upazilaId) { this.upazilaId = upazilaId; }

    public String getUnionId() { return unionId; }
    public void setUnionId(String unionId) { this.unionId = unionId; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    // Helper methods
    public int getAgeInMonths() {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) return 0;
        try {
            LocalDate dob = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE);
            Period period = Period.between(dob, LocalDate.now());
            return period.getYears() * 12 + period.getMonths();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getAgeString() {
        int months = getAgeInMonths();
        if (months < 12) {
            return months + " months";
        } else {
            int years = months / 12;
            int remainingMonths = months % 12;
            if (remainingMonths == 0) {
                return years + " year" + (years > 1 ? "s" : "");
            }
            return years + "y " + remainingMonths + "m";
        }
    }
}
