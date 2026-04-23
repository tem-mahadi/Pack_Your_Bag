package com.temmahadi.packyourbag.utils;

public final class PackingStatsUtils {

    private PackingStatsUtils() {
    }

    public static int getReadinessPercent(int packed, int total) {
        if (total <= 0 || packed <= 0) {
            return 0;
        }

        if (packed >= total) {
            return 100;
        }

        return Math.round((packed * 100f) / total);
    }
}
