package com.example.nutrimap.data.repository;

import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;
import com.example.nutrimap.util.StaticDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for child data access.
 */
public class ChildRepository {

    private static ChildRepository instance;

    private ChildRepository() {}

    public static synchronized ChildRepository getInstance() {
        if (instance == null) {
            instance = new ChildRepository();
        }
        return instance;
    }

    public List<Child> getAllChildren() {
        return StaticDataProvider.getInstance().getChildren();
    }

    public List<Child> searchChildren(String query) {
        if (query == null || query.isEmpty()) {
            return getAllChildren();
        }

        String lowerQuery = query.toLowerCase();
        List<Child> result = new ArrayList<>();
        for (Child c : getAllChildren()) {
            if (c.getName().toLowerCase().contains(lowerQuery) ||
                    c.getFatherName().toLowerCase().contains(lowerQuery) ||
                    c.getMotherName().toLowerCase().contains(lowerQuery)) {
                result.add(c);
            }
        }
        return result;
    }

    public Child getChildById(int id) {
        return StaticDataProvider.getInstance().getChildById(id);
    }

    public Child addChild(Child child) {
        return StaticDataProvider.getInstance().addChild(child);
    }

    public void updateChild(Child child) {
        StaticDataProvider.getInstance().updateChild(child);
    }

    public void deleteChild(int id) {
        StaticDataProvider.getInstance().deleteChild(id);
    }

    /**
     * Get risk level for a child based on their latest visit's MUAC.
     */
    public String getRiskLevelForChild(int childId) {
        Visit latestVisit = StaticDataProvider.getInstance().getLatestVisitForChild(childId);
        if (latestVisit == null) {
            return "N/A";
        }
        return NutritionRiskCalculator.calculateRiskFromMuac(latestVisit.getMuacMm());
    }

    /**
     * Get statistics for dashboard.
     */
    public DashboardStats getDashboardStats() {
        List<Child> children = getAllChildren();
        int total = children.size();
        int highRisk = 0;
        int mediumRisk = 0;
        int lowRisk = 0;

        for (Child c : children) {
            String risk = getRiskLevelForChild(c.getId());
            switch (risk) {
                case "High":
                    highRisk++;
                    break;
                case "Medium":
                    mediumRisk++;
                    break;
                case "Low":
                    lowRisk++;
                    break;
            }
        }

        return new DashboardStats(total, highRisk, mediumRisk, lowRisk);
    }

    public static class DashboardStats {
        public final int totalChildren;
        public final int highRisk;
        public final int mediumRisk;
        public final int lowRisk;

        public DashboardStats(int totalChildren, int highRisk, int mediumRisk, int lowRisk) {
            this.totalChildren = totalChildren;
            this.highRisk = highRisk;
            this.mediumRisk = mediumRisk;
            this.lowRisk = lowRisk;
        }
    }
}
