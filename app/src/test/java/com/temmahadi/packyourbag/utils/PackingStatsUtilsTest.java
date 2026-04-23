package com.temmahadi.packyourbag.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PackingStatsUtilsTest {

    @Test
    public void readiness_isZero_whenTotalIsZero() {
        assertEquals(0, PackingStatsUtils.getReadinessPercent(5, 0));
    }

    @Test
    public void readiness_isRounded_whenPartiallyPacked() {
        assertEquals(43, PackingStatsUtils.getReadinessPercent(3, 7));
    }

    @Test
    public void readiness_isCappedAtHundred() {
        assertEquals(100, PackingStatsUtils.getReadinessPercent(10, 8));
    }
}
