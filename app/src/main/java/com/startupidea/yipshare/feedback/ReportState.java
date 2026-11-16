package com.startupidea.yipshare.feedback;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.startupidea.yipshare.R;

import java.util.Locale;

public enum ReportState {
    PENDING(R.string.reporte_abierto, R.drawable.bg_badge_uso),
    IN_REVIEW(R.string.reporte_en_revision, R.drawable.bg_pendiente),
    RESOLVED(R.string.reporte_resuelto, R.drawable.bg_badge_disponible);

    private final int messageRes;
    private final int badgeRes;

    ReportState(@StringRes int messageRes, @DrawableRes int badgeRes) {
        this.messageRes = messageRes;
        this.badgeRes = badgeRes;
    }

    public static ReportState parse(String value) {
        return ReportState.valueOf(value.trim().toUpperCase(Locale.ROOT));
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
}
