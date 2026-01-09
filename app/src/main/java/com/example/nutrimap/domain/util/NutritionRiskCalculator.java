package com.example.nutrimap.domain.util;

/**
 * Calculates nutrition risk level based on MUAC (Mid-Upper Arm Circumference) measurement.
 * Uses WHO standards for acute malnutrition classification.
 */
public class NutritionRiskCalculator {

    // MUAC thresholds in millimeters
    public static final int MUAC_SEVERE_THRESHOLD = 115;  // < 115mm = Severe Acute Malnutrition (SAM)
    public static final int MUAC_MODERATE_THRESHOLD = 125; // < 125mm = Moderate Acute Malnutrition (MAM)

    /**
     * Calculate nutrition risk level from MUAC measurement.
     *
     * @param muacMm MUAC measurement in millimeters
     * @return Risk level: "High" (SAM), "Medium" (MAM), "Low" (Normal), or "N/A" if invalid
     */
    public static String calculateRiskFromMuac(int muacMm) {
        if (muacMm <= 0) {
            return "N/A";
        }
        if (muacMm < MUAC_SEVERE_THRESHOLD) {
            return "High";   // Severe Acute Malnutrition
        }
        if (muacMm < MUAC_MODERATE_THRESHOLD) {
            return "Medium"; // Moderate Acute Malnutrition
        }
        return "Low";        // Normal
    }

    /**
     * Get the color resource ID for a given risk level.
     * Used for UI display of risk badges.
     *
     * @param riskLevel Risk level string ("High", "Medium", "Low", or "N/A")
     * @return Resource ID for the background drawable
     */
    public static int getRiskBackgroundResource(String riskLevel) {
        switch (riskLevel) {
            case "High":
                return com.example.nutrimap.R.drawable.bg_risk_high;
            case "Medium":
                return com.example.nutrimap.R.drawable.bg_risk_medium;
            case "Low":
                return com.example.nutrimap.R.drawable.bg_risk_low;
            default:
                return com.example.nutrimap.R.drawable.bg_card_rounded;
        }
    }

    /**
     * Get the text color resource ID for a given risk level.
     *
     * @param riskLevel Risk level string
     * @return Resource ID for the text color
     */
    public static int getRiskTextColorResource(String riskLevel) {
        switch (riskLevel) {
            case "High":
                return com.example.nutrimap.R.color.colorRiskHigh;
            case "Medium":
                return com.example.nutrimap.R.color.colorRiskMedium;
            case "Low":
                return com.example.nutrimap.R.color.colorRiskLow;
            default:
                return com.example.nutrimap.R.color.colorTextSecondary;
        }
    }
}
