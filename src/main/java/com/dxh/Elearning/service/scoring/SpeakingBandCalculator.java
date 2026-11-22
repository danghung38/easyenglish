package com.dxh.Elearning.service.scoring;

import java.util.List;

/** IELTS Speaking overall band is the equally weighted mean of four criteria. */
public final class SpeakingBandCalculator {
    private SpeakingBandCalculator() {
    }

    public static double overallBand(
            Double fluencyCoherence,
            Double lexicalResource,
            Double grammaticalRangeAccuracy,
            Double pronunciation) {
        List<Double> values = List.of(
                requireBand(fluencyCoherence, "fluencyCoherence"),
                requireBand(lexicalResource, "lexicalResource"),
                requireBand(grammaticalRangeAccuracy, "grammaticalRange"),
                requireBand(pronunciation, "pronunciation"));
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        return Math.round(mean * 2.0) / 2.0;
    }

    private static double requireBand(Double value, String name) {
        if (value == null || value < 0 || value > 9 || Math.abs(value * 2 - Math.rint(value * 2)) > 1e-9) {
            throw new IllegalArgumentException(name + " must be a half-band value between 0 and 9");
        }
        return value;
    }
}
