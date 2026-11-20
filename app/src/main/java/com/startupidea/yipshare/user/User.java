package com.startupidea.yipshare.user;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

public record User(String id, String username) implements Parcelable {

    private static final String EXTRA_USER = "com.startupidea.yipshare.extra.USER";

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source.readString(), source.readString());
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static User fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, EXTRA_USER, User.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(EXTRA_USER, this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
    }
}
