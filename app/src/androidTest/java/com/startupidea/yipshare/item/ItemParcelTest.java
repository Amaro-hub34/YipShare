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
        assertEquals("id-1", item.getId());
        assertEquals("owner-1", item.getOwnerId());
        assertEquals("condo-1", item.getCondominiumId());
        assertEquals(ItemAvailability.PENDING, item.getAvailability());
        assertEquals("Drill", item.getPayload().getName());
        assertEquals("Tools", item.getPayload().getCategory());
        assertEquals("Works fine", item.getPayload().getDescription());
        assertEquals("Good", item.getPayload().getPhysicalCondition());
        assertEquals("content://image/1", item.getPayload().getImageUri());
        assertEquals(Arrays.asList("1 day", "1 week"), item.getPayload().getLoanDurations());
    }
}
