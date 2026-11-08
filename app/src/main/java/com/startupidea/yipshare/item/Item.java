package com.startupidea.yipshare.item;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

public final class Item implements Parcelable {

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

    private final String id;
    private final String ownerId;
    private final String condominiumId;
    private final ItemAvailability availability;
    private final ItemPayload payload;

    public Item(String id, String ownerId, String condominiumId, ItemAvailability availability, ItemPayload payload) {
        this.id = id;
        this.ownerId = ownerId;
        this.condominiumId = condominiumId;
        this.availability = availability;
        this.payload = payload;
    }

    public static Item fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, EXTRA_ITEM, Item.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(EXTRA_ITEM, this);
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getCondominiumId() {
        return condominiumId;
    }

    public ItemAvailability getAvailability() {
        return availability;
    }

    public boolean isOwnedBy(String userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    public ItemPayload getPayload() {
        return payload;
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
