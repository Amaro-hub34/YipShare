package com.startupidea.yipshare.condominium;

import java.io.Serializable;

public record CondominiumPayload(
        String name,
        String street,
        String neighborhood,
        String zipCode,
        String municipality,
        String city,
        String state,
        int units,
        String deedUri,
        String minutesUri,
        String taxIdUri,
        String identificationUri) implements Serializable {

    private static final long serialVersionUID = 1L;
}
