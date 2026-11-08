package com.startupidea.yipshare.loan;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

public final class Loan implements Parcelable {

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

    private final String id;
    private final String itemName;
    private final String description;
    private final String imageUri;
    private final String otherUserId;
    private final String otherUsername;
    private final boolean lentByMe;
    private final String startDate;
    private final String endDate;
    private final LoanStatus status;
    private final String otherPhoneNumber;

    public Loan(
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
            String otherPhoneNumber) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.imageUri = imageUri;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.lentByMe = lentByMe;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.otherPhoneNumber = otherPhoneNumber;
    }

    public static Loan fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, EXTRA_LOAN, Loan.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(EXTRA_LOAN, this);
    }

    public String getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    /** The other person of the loan: the borrower if the user lent it, the lender otherwise. */
    public String getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public boolean isLentByMe() {
        return lentByMe;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public String getOtherPhoneNumber() {
        return otherPhoneNumber;
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
