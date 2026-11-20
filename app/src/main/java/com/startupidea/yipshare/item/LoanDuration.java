package com.startupidea.yipshare.item;

public final class LoanDuration {

    private static final int[] DAYS = {1, 2, 3, 7, 14, 30};

    private LoanDuration() {
    }

    public static int daysOf(int index) {
        return DAYS[Math.max(0, Math.min(index, DAYS.length - 1))];
    }
}
