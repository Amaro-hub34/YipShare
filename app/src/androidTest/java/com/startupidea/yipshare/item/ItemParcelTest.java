package com.startupidea.yipshare.item;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class ItemParcelTest {

    @Test
    public void itemSurvivesIntentParceling() {
        Intent intent = new Intent();
        new Item("id-1", "owner-1", "condo-1", ItemAvailability.PENDING, new ItemPayload("Drill", "Tools",
                "Works fine", "Good",
                "content://image/1", Arrays.asList("1 day", "1 week"))).putInto(intent);

        Parcel parcel = Parcel.obtain();
        intent.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Intent restored = Intent.CREATOR.createFromParcel(parcel);
        restored.setExtrasClassLoader(Item.class.getClassLoader());
        parcel.recycle();

        Item item = Item.fromIntent(restored);
        assertEquals("id-1", item.id());
        assertEquals("owner-1", item.ownerId());
        assertEquals("condo-1", item.condominiumId());
        assertEquals(ItemAvailability.PENDING, item.availability());
        assertEquals("Drill", item.payload().getName());
        assertEquals("Tools", item.payload().getCategory());
        assertEquals("Works fine", item.payload().getDescription());
        assertEquals("Good", item.payload().getPhysicalCondition());
        assertEquals("content://image/1", item.payload().getImageUri());
        assertEquals(Arrays.asList("1 day", "1 week"), item.payload().getLoanDurations());
    }
}
