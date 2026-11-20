package com.startupidea.yipshare.user;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserParcelTest {

    @Test
    public void userSurvivesIntentParceling() {
        Intent intent = new Intent();
        new User("id-1", "ana").putInto(intent);

        Parcel parcel = Parcel.obtain();
        intent.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Intent restored = Intent.CREATOR.createFromParcel(parcel);
        restored.setExtrasClassLoader(User.class.getClassLoader());
        parcel.recycle();

        User user = User.fromIntent(restored);
        assertEquals("id-1", user.id());
        assertEquals("ana", user.username());
    }

    @Test
    public void draftSurvivesIntentParceling() {
        RegistrationDraft draft = RegistrationDraft.EMPTY
                .withNames("Pérez", "López", "Ana")
                .withCredentials("anitas", "Secret1!", "Secret1!", "5512345678", "ana@test.com");
        Intent intent = new Intent();
        draft.putInto(intent);

        Parcel parcel = Parcel.obtain();
        intent.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Intent restored = Intent.CREATOR.createFromParcel(parcel);
        restored.setExtrasClassLoader(RegistrationDraft.class.getClassLoader());
        parcel.recycle();

        RegistrationDraft result = RegistrationDraft.fromIntent(restored);
        assertEquals("Pérez", result.paternalLastName());
        assertEquals("López", result.maternalLastName());
        assertEquals("Ana", result.firstNames());
        assertEquals("anitas", result.username());
        assertEquals("Secret1!", result.password());
        assertEquals("Secret1!", result.passwordConfirmation());
        assertEquals("ana@test.com", result.email());
    }
}
