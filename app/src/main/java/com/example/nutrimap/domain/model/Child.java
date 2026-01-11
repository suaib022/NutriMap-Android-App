package com.example.nutrimap.domain.model;

import com.google.firebase.firestore.PropertyName;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Child {
    private int id;
    private String documentId; // Firestore document ID
    private String fullName;
    private String fathersName;
    private String mothersName;
    private String contactNumber;
    private String division; // Spec says division (name?) or divisionId? Spec says "division: String". Existing: divisionId. I'll add division, district, upazilla, unionName as per spec.
    private String district;
    private String upazilla;
    private String unionName;
    private String branchName;
    private String lastVisit; 
    
    // Keeping existing ID fields if needed for DB, but spec doesn't mention them explicitly in the data class snippet? 
    // Spec: division: String, district: String...
    // Existing: divisionId, districtId... 
    // I will KEEP existing ID fields to avoid breaking other code, and ADD the spec fields. 
    // Actually the spec defines the data class. 
    // Let's renaming existing fields to match spec where possible and add missing.
    
    private String gender;
    private String dateOfBirth;

    // Legacy/Existing IDs - Keeping them but maybe they are not needed if we follow spec exactly? 
    // The spec data class might be a "view" model or the actual entity. 
    // If I change 'name' to 'fullName', it might break existing code. 
    // I will refactor to match spec.
    
    // Re-declaring all fields to match spec EXACTLY + keep necessary IDs (like divisionId etc if they are distinct).
    // Spec: division: String. Existing: divisionId. 
    // I will assume spec 'division' means the name, but existing uses ID. 
    // I will add the spec fields.

    private String divisionId;
    private String districtId;
    private String upazilaId;
    private String unionId;
    private String branchId;

    public Child() {}

    public Child(int id, String fullName, String fathersName, String mothersName, String contactNumber,
                 String division, String district, String upazilla, String unionName,
                 String branchId, String branchName, String lastVisit,
                 String gender, String dateOfBirth,
                 // Extra params for existing ID fields
                 String divisionId, String districtId, String upazilaId, String unionId) {
        this.id = id;
        this.fullName = fullName;
        this.fathersName = fathersName;
        this.mothersName = mothersName;
        this.contactNumber = contactNumber;
        this.division = division;
        this.district = district;
        this.upazilla = upazilla;
        this.unionName = unionName;
        this.branchId = branchId;
        this.branchName = branchName;
        this.lastVisit = lastVisit;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        
        this.divisionId = divisionId;
        this.districtId = districtId;
        this.upazilaId = upazilaId;
        this.unionId = unionId;
    }

    // Getters and Setters matching Spec
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @PropertyName("name")
    public String getFullName() { return fullName; }
    @PropertyName("name")
    public void setFullName(String fullName) { this.fullName = fullName; }

    @PropertyName("fatherName")
    public String getFathersName() { return fathersName; }
    @PropertyName("fatherName")
    public void setFathersName(String fathersName) { this.fathersName = fathersName; }

    @PropertyName("motherName")
    public String getMothersName() { return mothersName; }
    @PropertyName("motherName")
    public void setMothersName(String mothersName) { this.mothersName = mothersName; }

    @PropertyName("contact")
    public String getContactNumber() { return contactNumber; }
    @PropertyName("contact")
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getUpazilla() { return upazilla; }
    public void setUpazilla(String upazilla) { this.upazilla = upazilla; }

    public String getUnionName() { return unionName; }
    public void setUnionName(String unionName) { this.unionName = unionName; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    // Legacy/ID Getters/Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    
    public String getDivisionId() { return divisionId; }
    public void setDivisionId(String divisionId) { this.divisionId = divisionId; }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    public String getUpazilaId() { return upazilaId; }
    public void setUpazilaId(String upazilaId) { this.upazilaId = upazilaId; }

    public String getUnionId() { return unionId; }
    public void setUnionId(String unionId) { this.unionId = unionId; }

    // Helpers
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
