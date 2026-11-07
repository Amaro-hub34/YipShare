package com.startupidea.yipshare.condominium;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CondominiumService {

    private static final Pattern ZIP_CODE = Pattern.compile("\\d{5}");

    private final CondominiumRepository condominiumRepository;

    public CondominiumService(CondominiumRepository condominiumRepository) {
        this.condominiumRepository = condominiumRepository;
    }

    public void validate(CondominiumPayload payload) {
        Set<CondominiumField> invalidFields = EnumSet.noneOf(CondominiumField.class);

        if (payload.getName().trim().isEmpty()) invalidFields.add(CondominiumField.NAME);
        if (payload.getStreet().trim().isEmpty()) invalidFields.add(CondominiumField.STREET);
        if (payload.getNeighborhood().trim().isEmpty()) invalidFields.add(CondominiumField.NEIGHBORHOOD);
        if (!ZIP_CODE.matcher(payload.getZipCode()).matches()) invalidFields.add(CondominiumField.ZIP_CODE);
        if (payload.getMunicipality().trim().isEmpty()) invalidFields.add(CondominiumField.MUNICIPALITY);
        if (payload.getCity().trim().isEmpty()) invalidFields.add(CondominiumField.CITY);
        if (payload.getUnits() <= 0) invalidFields.add(CondominiumField.UNITS);
        if (payload.getDeedUri() == null) invalidFields.add(CondominiumField.DEED);
        if (payload.getMinutesUri() == null) invalidFields.add(CondominiumField.MINUTES);
        if (payload.getTaxIdUri() == null) invalidFields.add(CondominiumField.TAX_ID);
        if (payload.getIdentificationUri() == null) invalidFields.add(CondominiumField.IDENTIFICATION);

        if (!invalidFields.isEmpty()) throw new InvalidCondominium(invalidFields);
    }

    public void createCondominiumRequest(CondominiumPayload payload) {
        validate(payload);
        condominiumRepository.createCondominiumRequest(payload);
    }
}
