package com.dxh.Elearning.service.scoring;

import java.util.List;

/** Deterministic IELTS Writing score calculations owned by the backend. */
public final class WritingBandCalculator {

    private WritingBandCalculator() {
    }

    public static double taskBand(
            Double taskCriterion,
            Double coherenceCohesion,
            Double lexicalResource,
            Double grammaticalRangeAccuracy
    ) {
        List<Double> criteria = List.of(
                requireBand(taskCriterion, "taskCriterion"),
                requireBand(coherenceCohesion, "coherenceCohesion"),
                requireBand(lexicalResource, "lexicalResource"),
                requireBand(grammaticalRangeAccuracy, "grammaticalRangeAccuracy")
        );
        return roundToHalf(criteria.stream().mapToDouble(Double::doubleValue).average().orElseThrow());
    }

    public static double overallBand(double task1Band, double task2Band) {
        requireBand(task1Band, "task1Band");
        requireBand(task2Band, "task2Band");
        return roundToHalf((task1Band + 2.0 * task2Band) / 3.0);
    }

    public static double roundToHalf(double value) {
        if (!Double.isFinite(value) || value < 0.0 || value > 9.0) {
            throw new IllegalArgumentException("Band must be finite and between 0 and 9");
        }
        return Math.round(value * 2.0) / 2.0;
    }

    private static double requireBand(Double value, String name) {
        if (value == null || !Double.isFinite(value) || value < 0.0 || value > 9.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 9");
        }
        if (Math.abs(value * 2.0 - Math.rint(value * 2.0)) > 1e-9) {
            throw new IllegalArgumentException(name + " must use 0.5 increments");
        }
        return value;
    }
}
