package com.startupidea.yipshare.condominium;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class CondominiumService {

    private static final Pattern ZIP_CODE = Pattern.compile("\\d{5}");
    private static final Pattern SHORT_CODE = Pattern.compile("[A-Z0-9]{6,8}");

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
        if (payload.getState().trim().isEmpty()) invalidFields.add(CondominiumField.STATE);
        if (payload.getUnits() <= 0) invalidFields.add(CondominiumField.UNITS);
        if (payload.getDeedUri() == null) invalidFields.add(CondominiumField.DEED);
        if (payload.getMinutesUri() == null) invalidFields.add(CondominiumField.MINUTES);
        if (payload.getTaxIdUri() == null) invalidFields.add(CondominiumField.TAX_ID);
        if (payload.getIdentificationUri() == null) invalidFields.add(CondominiumField.IDENTIFICATION);

        if (!invalidFields.isEmpty()) throw new InvalidCondominium(invalidFields);
    }

    public void createCondominiumRequest(String userId, CondominiumPayload payload) {
        validate(payload);
        condominiumRepository.createCondominiumRequest(payload);
    }

    public void joinCondominium(String userId, String shortCode, String building, String apartment) {
        String code = shortCode.trim().toUpperCase(Locale.ROOT).replace("-", "");
        if (!SHORT_CODE.matcher(code).matches()) throw new CondominiumNotFound();
        if (apartment.trim().isEmpty()) throw new EmptyApartment();
        String unit = building.trim().isEmpty() ? apartment.trim() : building.trim() + " " + apartment.trim();
        condominiumRepository.createPendingMember(code, unit);
    }

    public List<CondominiumMembership> getAllMemberships(String userId) {
        return condominiumRepository.getAllMemberships();
    }

    public boolean isAdmin(String userId, String condominiumId) {
        CondominiumMembership membership = findMembership(condominiumId);
        return membership != null && membership.isAdmin();
    }

    public List<Neighbor> getAllNeighbors(String adminId, String condominiumId) {
        ensureAdmin(adminId, condominiumId);
        return condominiumRepository.getAllNeighborsByCondominiumId(condominiumId);
    }

    public List<JoinRequest> getAllJoinRequests(String adminId, String condominiumId) {
        CondominiumMembership membership = findMembership(condominiumId);
        if (membership == null || !membership.isAdmin()) return new ArrayList<>();
        return condominiumRepository.getAllJoinRequestsByCondominiumId(condominiumId, membership.getName());
    }

    public void acceptJoinRequest(String adminId, JoinRequest request, String unitId) {
        condominiumRepository.acceptJoinRequest(request.getUserId(), request.getCondominiumId(),
                unitId == null ? request.getSuggestedUnitId() : unitId);
    }

    public void rejectJoinRequest(String adminId, String userId, String condominiumId) {
        condominiumRepository.rejectJoinRequest(userId, condominiumId);
    }

    public void modifyLoanAccess(String adminId, String userId, String condominiumId, boolean allowed) {
        if (adminId.equals(userId)) throw new NotCondominiumAdmin();
        condominiumRepository.modifyLoanAccess(userId, condominiumId, allowed);
    }

    public List<Unit> getAllUnits(String condominiumId) {
        if (condominiumId == null) return new ArrayList<>();
        return condominiumRepository.getAllUnitsByCondominiumId(condominiumId);
    }

    public void createUnit(String condominiumId, String name) {
        if (name.trim().isEmpty()) throw new EmptyApartment();
        condominiumRepository.createUnit(condominiumId, name.trim());
    }

    public void modifyUnit(String condominiumId, Unit unit, String name) {
        if (name.trim().isEmpty()) throw new EmptyApartment();
        condominiumRepository.modifyUnit(condominiumId, unit.getId(), name.trim());
    }

    public void deleteUnit(String condominiumId, Unit unit) {
        condominiumRepository.deleteUnit(condominiumId, unit.getId());
    }

    private CondominiumMembership findMembership(String condominiumId) {
        if (condominiumId == null) return null;
        for (CondominiumMembership membership : condominiumRepository.getAllMemberships()) {
            if (membership.getCondominiumId().equals(condominiumId)) return membership;
        }
        return null;
    }

    private void ensureAdmin(String adminId, String condominiumId) {
        if (!isAdmin(adminId, condominiumId)) throw new NotCondominiumAdmin();
    }
}
