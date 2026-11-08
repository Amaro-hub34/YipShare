package com.startupidea.yipshare.item;
import com.startupidea.yipshare.R;

/** Days of each entry of R.array.tiempo_prestamo, in the same order. */
public final class LoanDuration {

    private static final int[] DAYS = {1, 2, 3, 7, 14, 30};

    private LoanDuration() {
    }

    public static int daysOf(int index) {
        return DAYS[Math.max(0, Math.min(index, DAYS.length - 1))];
    }
}
