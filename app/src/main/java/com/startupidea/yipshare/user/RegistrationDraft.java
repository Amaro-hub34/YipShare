package com.startupidea.yipshare.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;
import androidx.core.os.BundleCompat;

public final class RegistrationDraft implements Parcelable {

    private static final String KEY_DRAFT = "com.startupidea.yipshare.extra.REGISTRATION_DRAFT";

    public static final RegistrationDraft EMPTY = new RegistrationDraft("", "", "", "", "", "", "", "");

    public static final Creator<RegistrationDraft> CREATOR = new Creator<RegistrationDraft>() {
        @Override
        public RegistrationDraft createFromParcel(Parcel source) {
            return new RegistrationDraft(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString());
        }

        @Override
        public RegistrationDraft[] newArray(int size) {
            return new RegistrationDraft[size];
        }
    };

    private final String paternalLastName;
    private final String maternalLastName;
    private final String firstNames;
    private final String username;
    private final String password;
    private final String passwordConfirmation;
    private final String phoneNumber;
    private final String email;

    private RegistrationDraft(
            String paternalLastName,
            String maternalLastName,
            String firstNames,
            String username,
            String password,
            String passwordConfirmation,
            String phoneNumber,
            String email) {
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
        this.firstNames = firstNames;
        this.username = username;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public static RegistrationDraft fromIntent(Intent intent) {
        return IntentCompat.getParcelableExtra(intent, KEY_DRAFT, RegistrationDraft.class);
    }

    public static RegistrationDraft fromBundle(Bundle bundle) {
        return BundleCompat.getParcelable(bundle, KEY_DRAFT, RegistrationDraft.class);
    }

    public void putInto(Intent intent) {
        intent.putExtra(KEY_DRAFT, this);
    }

    public void putInto(Bundle bundle) {
        bundle.putParcelable(KEY_DRAFT, this);
    }

    public RegistrationDraft withNames(String paternalLastName, String maternalLastName, String firstNames) {
        return new RegistrationDraft(
                paternalLastName,
                maternalLastName,
                firstNames,
                username,
                password,
                passwordConfirmation,
                phoneNumber,
                email);
    }

    public RegistrationDraft withCredentials(String username, String password, String passwordConfirmation, String phoneNumber,
                                             String email) {
        return new RegistrationDraft(
                paternalLastName,
                maternalLastName,
                firstNames,
                username,
                password,
                passwordConfirmation,
                phoneNumber,
                email);
    }

    public RegistrationPayload toPayload() {
        return new RegistrationPayload(
                paternalLastName.trim(),
                maternalLastName.trim(),
                firstNames.trim(),
                username.trim(),
                password,
                UserRules.normalizePhoneNumber(phoneNumber),
                email.trim());
    }

    public String getPaternalLastName() {
        return paternalLastName;
    }

    public String getMaternalLastName() {
        return maternalLastName;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(paternalLastName);
        dest.writeString(maternalLastName);
        dest.writeString(firstNames);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(passwordConfirmation);
        dest.writeString(phoneNumber);
        dest.writeString(email);
    }
}
