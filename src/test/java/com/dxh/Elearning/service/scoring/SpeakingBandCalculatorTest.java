package com.dxh.Elearning.service.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpeakingBandCalculatorTest {
    @Test
    void calculatesEqualCriterionMeanWithoutPartWeights() {
        assertEquals(6.5, SpeakingBandCalculator.overallBand(6.0, 6.5, 6.5, 7.0));
    }

    @Test
    void rejectsMissingPronunciationEvidence() {
        assertThrows(IllegalArgumentException.class,
                () -> SpeakingBandCalculator.overallBand(6.0, 6.5, 6.5, null));
    }
}
