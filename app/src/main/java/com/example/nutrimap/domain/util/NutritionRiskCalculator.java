package com.example.nutrimap.domain.util;

import com.example.nutrimap.R;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class NutritionRiskCalculator {

    // Constants (MUST MATCH EXACTLY)
    public static final double MUAC_SEVERE = 11.5;
    public static final double MUAC_MODERATE = 12.5;
    public static final double Z_SEVERE = -3.0;
    public static final double Z_MODERATE = -2.0;

    public static final String NUTRITION_SEVERE = "severe malnutrition";
    public static final String NUTRITION_MODERATE = "moderate malnutrition";
    public static final String NUTRITION_NORMAL = "normal";

    public static final String RISK_HIGH = "high";
    public static final String RISK_MEDIUM = "medium";
    public static final String RISK_LOW = "low";
    public static final String RISK_NA = "N/A";

    public static class RiskResult {
        public String riskLevel;       // "high", "medium", "low"
        public String nutritionLevel;  // "severe malnutrition", etc.
        public double zScore;          // The calculated z-score (or NaN)
        public int score;              // The B2 score

        public RiskResult(String riskLevel, String nutritionLevel, double zScore, int score) {
            this.riskLevel = riskLevel;
            this.nutritionLevel = nutritionLevel;
            this.zScore = zScore;
            this.score = score;
        }
    }

    /**
     * Evaluate nutrition and risk level based on the current visit and optional previous visit.
     */
    public static RiskResult evaluateFromVisitData(Visit current, Visit previous, Child child) {
        if (current == null || child == null) {
            return new RiskResult(RISK_NA, NUTRITION_NORMAL, Double.NaN, 0);
        }

        // 1. Calculate Age at Visit
        int ageMonths = calculateAgeInMonthsAtVisit(child.getDateOfBirth(), current.getVisitDate());
        
        // 2. Prepare Metrics
        double weightKg = current.getWeightKg();
        double heightCm = current.getHeightCm();
        double muacCm = current.getMuacMm() / 10.0;
        String gender = child.getGender();

        // 3. Compute WHO Z-score
        double zScore = WhoGrowthStandards.computeWhzZScore(weightKg, heightCm, gender);

        // 4. Determine Nutrition Level (A1 + A2)
        // Severe: MUAC < 11.5 OR z < -3
        // Moderate: 11.5 <= MUAC < 12.5 OR -3 <= z < -2
        // Normal: otherwise
        
        boolean isSevere = (muacCm < MUAC_SEVERE) || (!Double.isNaN(zScore) && zScore < Z_SEVERE);
        boolean isModerate = false;
        
        if (!isSevere) {
            // Check Moderate
            boolean muacMod = (muacCm >= MUAC_SEVERE && muacCm < MUAC_MODERATE); 
            boolean zMod = (!Double.isNaN(zScore) && zScore >= Z_SEVERE && zScore < Z_MODERATE);
            
            if (muacMod || zMod) {
                isModerate = true;
            }
        }

        String nutritionLevel;
        int nutritionScore;

        if (isSevere) {
            nutritionLevel = NUTRITION_SEVERE;
            nutritionScore = 3;
        } else if (isModerate) {
            nutritionLevel = NUTRITION_MODERATE;
            nutritionScore = 2;
        } else {
            nutritionLevel = NUTRITION_NORMAL;
            nutritionScore = 1;
        }

        // 5. Calculate Risk Points (B2)
        int riskPoints = nutritionScore;

        // Age Factor: < 24 months -> +1
        if (ageMonths < 24) {
            riskPoints += 1;
        }

        // Borderline MUAC: 11.5–11.9 OR 12.5–12.9 -> +1
        boolean isBorderlineMuac = (muacCm >= 11.5 && muacCm < 12.0) || (muacCm >= 12.5 && muacCm < 13.0);
        if (isBorderlineMuac) {
            riskPoints += 1;
        }

        // Trend Analysis
        if (previous != null) {
            // MUAC drop >= 0.5cm
            // Ensure previous MUAC is computed same way (mm to cm)
            double prevMuacCm = previous.getMuacMm() / 10.0;
            if ((prevMuacCm - muacCm) >= 0.5) {
                riskPoints += 1;
            }

            // Weight loss >= 5%
            double prevWeight = previous.getWeightKg();
            double currentWeight = current.getWeightKg();
            if (prevWeight > 0) {
                double percentLoss = ((prevWeight - currentWeight) / prevWeight) * 100.0;
                if (percentLoss >= 5.0) {
                    riskPoints += 1;
                }
            }
        }

        // 6. Final Logic
        String riskLevel;
        if (riskPoints >= 4) {
            riskLevel = RISK_HIGH;
        } else if (riskPoints >= 2) { // 2-3
            riskLevel = RISK_MEDIUM;
        } else { // <= 1
            riskLevel = RISK_LOW;
        }

        return new RiskResult(riskLevel, nutritionLevel, zScore, riskPoints);
    }
    
    public static int calculateAgeInMonthsAtVisit(String dobString, String visitDateString) {
        if (dobString == null || visitDateString == null) return 0;
        try {
            LocalDate dob = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate visit = LocalDate.parse(visitDateString, DateTimeFormatter.ISO_LOCAL_DATE);
            Period p = Period.between(dob, visit);
            return p.getYears() * 12 + p.getMonths();
        } catch (Exception e) {
            return 0;
        }
    }

    // Helper methods for UI (Legacy support / Quick binding)
    public static int getRiskTextColorResource(String riskLevel) {
        if (riskLevel == null) return R.color.colorRiskNA;
        switch (riskLevel.toLowerCase()) {
            case "high": return R.color.colorRiskHigh;
            case "medium": return R.color.colorRiskMedium;
            case "low": return R.color.colorRiskLow;
            default: return R.color.colorRiskNA;
        }
    }

    public static int getRiskBackgroundResource(String riskLevel) {
        if (riskLevel == null) return R.color.colorRiskNA; // Fallback
        switch (riskLevel.toLowerCase()) {
            case "high": return R.color.colorRiskHighBackground;
            case "medium": return R.color.colorRiskMediumBackground;
            case "low": return R.color.colorRiskLowBackground;
            default: return R.color.colorSurfaceVariant;
        }
    }
}
