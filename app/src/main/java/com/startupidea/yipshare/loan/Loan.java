package com.startupidea.yipshare.loan;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

public record Loan(
        String id,
        String itemName,
        String description,
        String imageUri,
        String otherUserId,
        String otherUsername,
        boolean lentByMe,
        String startDate,
        String endDate,
        LoanStatus status,
        String otherPhoneNumber) implements Parcelable {

    private static final String EXTRA_LOAN = "com.startupidea.yipshare.extra.LOAN";

    public static final Creator<Loan> CREATOR = new Creator<Loan>() {
        @Override
        public Loan createFromParcel(Parcel source) {
            return new Loan(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readInt() == 1,
                    source.readString(),
                    source.readString(),
                    LoanStatus.valueOf(source.readString()),
                    source.readString());
        }

        @Override
        public Loan[] newArray(int size) {
            return new Loan[size];
        }
    };

    public static Loan fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, EXTRA_LOAN, Loan.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(EXTRA_LOAN, this);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(itemName);
        dest.writeString(description);
        dest.writeString(imageUri);
        dest.writeString(otherUserId);
        dest.writeString(otherUsername);
        dest.writeInt(lentByMe ? 1 : 0);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(status.name());
        dest.writeString(otherPhoneNumber);
    }
}
