package com.startupidea.yipshare.condominium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.EnumSet;

public class CondominiumServiceTest {

    private static final String PDF = "content://docs/1";

    private CondominiumPayload sent;
    private final CondominiumService service = new CondominiumService(payload -> sent = payload);

    private static CondominiumPayload payload(String zipCode, int units, String deed) {
        return new CondominiumPayload("Torres", "Av. 1", "Centro", zipCode, "Cuauhtémoc", "CDMX",
                units, deed, PDF, PDF, PDF);
    }

    private void assertInvalid(CondominiumPayload payload, EnumSet<CondominiumField> expected) {
        InvalidCondominium error = assertThrows(InvalidCondominium.class,
                () -> service.createCondominiumRequest(payload));
        assertEquals(expected, error.getFields());
    }

    @Test
    public void sendsValidRequest() {
        CondominiumPayload payload = payload("06000", 10, PDF);

        service.createCondominiumRequest(payload);

        assertSame(payload, sent);
    }

    @Test
    public void rejectsZipCodeWithoutFiveDigits() {
        assertInvalid(payload("0600", 10, PDF), EnumSet.of(CondominiumField.ZIP_CODE));
        assertInvalid(payload("060001", 10, PDF), EnumSet.of(CondominiumField.ZIP_CODE));
        assertInvalid(payload("06a00", 10, PDF), EnumSet.of(CondominiumField.ZIP_CODE));
    }

    @Test
    public void rejectsMissingUnitsAndDocument() {
        assertInvalid(payload("06000", 0, null), EnumSet.of(CondominiumField.UNITS, CondominiumField.DEED));
    }
}
