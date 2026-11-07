package com.startupidea.yipshare.item;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

import java.io.Serializable;

public final class Item implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    private static final String EXTRA_ITEM = "com.startupidea.yipshare.extra.ITEM";

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(
                    source.readString(),
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
    private final ItemPayload payload;

    public Item(String id, ItemPayload payload) {
        this.id = id;
        this.payload = payload;
    }

    public static Item fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, EXTRA_ITEM, Item.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(EXTRA_ITEM, (Parcelable) this);
    }

    public String getId() {
        return id;
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
        dest.writeString(payload.getName());
        dest.writeString(payload.getCategory());
        dest.writeString(payload.getDescription());
        dest.writeString(payload.getPhysicalCondition());
        dest.writeString(payload.getImageUri());
        dest.writeStringList(payload.getLoanDurations());
    }
}
