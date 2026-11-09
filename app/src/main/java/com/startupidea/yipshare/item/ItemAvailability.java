package com.startupidea.yipshare.item;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.startupidea.yipshare.R;

// Same values as article_availability in the database schema.
public enum ItemAvailability {
    AVAILABLE(R.string.disponibilidad_disponible, R.drawable.bg_badge_disponible),
    NOT_AVAILABLE(R.string.disponibilidad_no_disponible, R.drawable.bg_badge_uso),
    PENDING(R.string.disponibilidad_pendiente, R.drawable.bg_pendiente);

    private final int messageRes;
    private final int badgeRes;

    ItemAvailability(@StringRes int messageRes, @DrawableRes int badgeRes) {
        this.messageRes = messageRes;
        this.badgeRes = badgeRes;
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
