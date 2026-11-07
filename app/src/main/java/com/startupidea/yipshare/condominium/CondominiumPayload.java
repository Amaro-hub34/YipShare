package com.startupidea.yipshare.condominium;

import java.io.Serializable;

public final class CondominiumPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String street;
    private final String neighborhood;
    private final String zipCode;
    private final String municipality;
    private final String city;
    private final int units;
    private final String deedUri;
    private final String minutesUri;
    private final String taxIdUri;
    private final String identificationUri;

    public CondominiumPayload(
            String name,
            String street,
            String neighborhood,
            String zipCode,
            String municipality,
            String city,
            int units,
            String deedUri,
            String minutesUri,
            String taxIdUri,
            String identificationUri) {
        this.name = name;
        this.street = street;
        this.neighborhood = neighborhood;
        this.zipCode = zipCode;
        this.municipality = municipality;
        this.city = city;
        this.units = units;
        this.deedUri = deedUri;
        this.minutesUri = minutesUri;
        this.taxIdUri = taxIdUri;
        this.identificationUri = identificationUri;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getCity() {
        return city;
    }

    public int getUnits() {
        return units;
    }

    public String getDeedUri() {
        return deedUri;
    }

    public String getMinutesUri() {
        return minutesUri;
    }

    public String getTaxIdUri() {
        return taxIdUri;
    }

    public String getIdentificationUri() {
        return identificationUri;
    }
}
