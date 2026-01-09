package com.example.nutrimap.data.repository;

import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;
import com.example.nutrimap.util.StaticDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for visit data access.
 */
public class VisitRepository {

    private static VisitRepository instance;

    private VisitRepository() {}

    public static synchronized VisitRepository getInstance() {
        if (instance == null) {
            instance = new VisitRepository();
        }
        return instance;
    }

    public List<Visit> getAllVisits() {
        return StaticDataProvider.getInstance().getVisits();
    }

    public List<Visit> searchVisits(String query) {
        if (query == null || query.isEmpty()) {
            return getAllVisits();
        }

        String lowerQuery = query.toLowerCase();
        List<Visit> result = new ArrayList<>();
        for (Visit v : getAllVisits()) {
            // Search by child name
            Child child = StaticDataProvider.getInstance().getChildById(v.getChildId());
            if (child != null && child.getName().toLowerCase().contains(lowerQuery)) {
                result.add(v);
            } else if (v.getVisitDate().contains(lowerQuery)) {
                result.add(v);
            } else if (v.getNotes() != null && v.getNotes().toLowerCase().contains(lowerQuery)) {
                result.add(v);
            }
        }
        return result;
    }

    public List<Visit> getVisitsForChild(int childId) {
        return StaticDataProvider.getInstance().getVisitsForChild(childId);
    }

    public Visit getLatestVisitForChild(int childId) {
        return StaticDataProvider.getInstance().getLatestVisitForChild(childId);
    }

    public Visit addVisit(Visit visit) {
        return StaticDataProvider.getInstance().addVisit(visit);
    }

    public void updateVisit(Visit visit) {
        StaticDataProvider.getInstance().updateVisit(visit);
    }

    public int getTotalVisitCount() {
        return getAllVisits().size();
    }

    /**
     * Get visits per month for chart display.
     * @return Map of month (yyyy-MM) to visit count
     */
    public Map<String, Integer> getVisitsPerMonth() {
        Map<String, Integer> result = new HashMap<>();
        for (Visit v : getAllVisits()) {
            String month = v.getVisitDate().substring(0, 7); // yyyy-MM
            result.put(month, result.getOrDefault(month, 0) + 1);
        }
        return result;
    }

    /**
     * Get child name for a visit.
     */
    public String getChildNameForVisit(int childId) {
        Child child = StaticDataProvider.getInstance().getChildById(childId);
        return child != null ? child.getName() : "Unknown";
    }

    /**
     * Calculate risk level for a visit.
     */
    public String getRiskLevelForVisit(Visit visit) {
        return NutritionRiskCalculator.calculateRiskFromMuac(visit.getMuacMm());
    }
}
