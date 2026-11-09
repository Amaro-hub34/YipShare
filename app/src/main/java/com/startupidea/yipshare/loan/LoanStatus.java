package com.startupidea.yipshare.loan;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.startupidea.yipshare.R;

import java.util.Locale;

// Same values as loan_state in the database schema.
public enum LoanStatus {
    PENDING(R.string.estado_solicitud_pendiente, R.drawable.bg_badge_uso),
    ACCEPTED(R.string.estado_pendiente, R.drawable.bg_badge_disponible),
    REJECTED(R.string.estado_rechazado, R.drawable.bg_badge_uso),
    ON_LOAN(R.string.estado_en_prestamo, R.drawable.bg_badge_uso),
    FINISHED(R.string.estado_finalizado, R.drawable.bg_badge_disponible);

    private final int messageRes;
    private final int badgeRes;

    LoanStatus(@StringRes int messageRes, @DrawableRes int badgeRes) {
        this.messageRes = messageRes;
        this.badgeRes = badgeRes;
    }

    public static LoanStatus parse(String value) {
        return LoanStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public String getValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    @StringRes
    public int getMessageRes() {
        return messageRes;
    }

    @DrawableRes
    public int getBadgeRes() {
        return badgeRes;
    }

    public boolean acceptsCode() {
        return this == ACCEPTED || this == ON_LOAN;
    }

    public LoanStatus next() {
        return this == ACCEPTED ? ON_LOAN : FINISHED;
    }
}
