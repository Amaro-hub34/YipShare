package com.startupidea.yipshare.item;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

public record Item(
        String id,
        String ownerId,
        String condominiumId,
        ItemAvailability availability,
        ItemPayload payload) implements Parcelable {

    private static final String EXTRA_ITEM = "com.startupidea.yipshare.extra.ITEM";

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    ItemAvailability.valueOf(source.readString()),
                    new ItemPayload(
                            source.readString(),
                            source.readString(),
                            source.readString(),
                            source.readString(),
                            source.readString(),
                            source.createStringArrayList()));
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public static Item fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, EXTRA_ITEM, Item.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(EXTRA_ITEM, this);
    }

    public boolean isOwnedBy(String userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(ownerId);
        dest.writeString(condominiumId);
        dest.writeString(availability.name());
        dest.writeString(payload.getName());
        dest.writeString(payload.getCategory());
        dest.writeString(payload.getDescription());
        dest.writeString(payload.getPhysicalCondition());
        dest.writeString(payload.getImageUri());
        dest.writeStringList(payload.getLoanDurations());
    }
}
