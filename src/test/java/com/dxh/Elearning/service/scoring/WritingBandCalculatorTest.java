package com.dxh.Elearning.service.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WritingBandCalculatorTest {

    @Test
    void calculatesTaskBandFromFourCriteria() {
        assertEquals(6.5, WritingBandCalculator.taskBand(6.0, 6.5, 7.0, 6.5));
    }

    @Test
    void task2HasDoubleWeight() {
        assertEquals(7.5, WritingBandCalculator.overallBand(6.5, 8.0));
    }

    @Test
    void rejectsMissingOrNonHalfBandCriteria() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WritingBandCalculator.taskBand(null, 6.5, 7.0, 6.5)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WritingBandCalculator.taskBand(6.2, 6.5, 7.0, 6.5)
        );
    }
}
