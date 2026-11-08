package com.startupidea.yipshare.condominium;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CondominiumService {

    private static final Pattern ZIP_CODE = Pattern.compile("\\d{5}");
    private static final Pattern SHORT_CODE = Pattern.compile("[A-Z0-9]{6}");

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

    public void createCondominiumRequest(String userId, CondominiumPayload payload) {
        validate(payload);
        condominiumRepository.createCondominiumRequest(userId, payload);
    }

    public void joinCondominium(String userId, String shortCode, String building, String apartment) {
        String code = shortCode.trim().toUpperCase();
        if (!SHORT_CODE.matcher(code).matches()) throw new CondominiumNotFound();
        if (apartment.trim().isEmpty()) throw new EmptyApartment();

        String condominiumId = condominiumRepository.getApprovedCondominiumIdByCode(code);
        if (condominiumId == null) throw new CondominiumNotFound();
        if (condominiumRepository.existsMembership(userId, condominiumId)) throw new AlreadyJoined();

        condominiumRepository.createPendingMember(userId, condominiumId, building.trim(), apartment.trim());
    }

    public List<CondominiumMembership> getAllMemberships(String userId) {
        // shortcut: approval only happens on the web, so every user gets a demo condominium they administer
        // and another one they live in; remove once condominiums are approved for real.
        if (!condominiumRepository.existsMembershipByUserId(userId)) condominiumRepository.createDemoCondominiums(userId);
        return condominiumRepository.getAllMembershipsByUserId(userId);
    }

    public boolean isAdmin(String userId, String condominiumId) {
        return condominiumId != null && condominiumRepository.isCondominiumAdmin(userId, condominiumId);
    }

    public boolean canRequestLoans(String userId, String condominiumId) {
        return condominiumRepository.canRequestLoans(userId, condominiumId);
    }

    public List<Neighbor> getAllNeighbors(String adminId, String condominiumId) {
        ensureAdmin(adminId, condominiumId);
        return condominiumRepository.getAllNeighborsByCondominiumId(condominiumId);
    }

    /** Only the requests of the selected condominium, and only if the user administers it. */
    public List<JoinRequest> getAllJoinRequests(String adminId, String condominiumId) {
        if (condominiumId == null) return new ArrayList<>();
        return condominiumRepository.getAllJoinRequestsByAdminId(adminId, condominiumId);
    }

    public void acceptJoinRequest(String adminId, String userId, String condominiumId) {
        ensureJoinRequest(adminId, userId, condominiumId);
        condominiumRepository.acceptJoinRequest(userId, condominiumId);
    }

    public void rejectJoinRequest(String adminId, String userId, String condominiumId) {
        ensureJoinRequest(adminId, userId, condominiumId);
        condominiumRepository.rejectJoinRequest(userId, condominiumId);
    }

    /** E.g. a neighbor who hasn't paid maintenance can't borrow until the admin gives access back. */
    public void modifyLoanAccess(String adminId, String userId, String condominiumId, boolean allowed) {
        ensureAdmin(adminId, condominiumId);
        if (adminId.equals(userId)) throw new NotCondominiumAdmin();
        condominiumRepository.modifyLoanAccess(userId, condominiumId, allowed);
    }

    private void ensureAdmin(String adminId, String condominiumId) {
        if (!isAdmin(adminId, condominiumId)) throw new NotCondominiumAdmin();
    }

    private void ensureJoinRequest(String adminId, String userId, String condominiumId) {
        ensureAdmin(adminId, condominiumId);
        if (!condominiumRepository.existsJoinRequest(userId, condominiumId)) throw new JoinRequestNotFound();
    }
}
