package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
import com.startupidea.yipshare.error.ResourceNotFound;
import com.startupidea.yipshare.error.UnauthorizedException;
import java.util.Optional;
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

        if (payload.name().trim().isEmpty()) invalidFields.add(CondominiumField.NAME);
        if (payload.street().trim().isEmpty()) invalidFields.add(CondominiumField.STREET);
        if (payload.neighborhood().trim().isEmpty()) invalidFields.add(CondominiumField.NEIGHBORHOOD);
        if (!ZIP_CODE.matcher(payload.zipCode()).matches()) invalidFields.add(CondominiumField.ZIP_CODE);
        if (payload.municipality().trim().isEmpty()) invalidFields.add(CondominiumField.MUNICIPALITY);
        if (payload.city().trim().isEmpty()) invalidFields.add(CondominiumField.CITY);
        if (payload.state().trim().isEmpty()) invalidFields.add(CondominiumField.STATE);
        if (payload.units() <= 0) invalidFields.add(CondominiumField.UNITS);
        if (payload.deedUri() == null) invalidFields.add(CondominiumField.DEED);
        if (payload.minutesUri() == null) invalidFields.add(CondominiumField.MINUTES);
        if (payload.taxIdUri() == null) invalidFields.add(CondominiumField.TAX_ID);
        if (payload.identificationUri() == null) invalidFields.add(CondominiumField.IDENTIFICATION);

        if (!invalidFields.isEmpty()) throw new InvalidCondominium(invalidFields);
    }

    public void createCondominiumRequest(String userId, CondominiumPayload payload) {
        validate(payload);
        condominiumRepository.createCondominiumRequest(payload);
    }

    public void joinCondominium(String userId, String shortCode, String building, String apartment) {
        String code = shortCode.trim().toUpperCase(Locale.ROOT).replace("-", "");
        if (!SHORT_CODE.matcher(code).matches()) throw new ResourceNotFound("No approved condominium has that code", R.string.tv_error_codigo);
        if (apartment.trim().isEmpty()) throw new BadRequestException("Apartment is empty", R.string.error_campo_obligatorio);
        String unit = building.trim().isEmpty() ? apartment.trim() : building.trim() + " " + apartment.trim();
        condominiumRepository.createPendingMember(code, unit);
    }

    public List<CondominiumMembership> getAllMemberships(String userId) {
        return condominiumRepository.getAllMemberships();
    }

    public boolean isAdmin(String userId, String condominiumId) {
        return findMembership(condominiumId).map(CondominiumMembership::admin).orElse(false);
    }

    public List<Neighbor> getAllNeighbors(String adminId, String condominiumId) {
        ensureAdmin(adminId, condominiumId);
        return condominiumRepository.getAllNeighborsByCondominiumId(condominiumId);
    }

    public List<JoinRequest> getAllJoinRequests(String adminId, String condominiumId) {
        return findMembership(condominiumId)
                .filter(CondominiumMembership::admin)
                .map(membership -> condominiumRepository.getAllJoinRequestsByCondominiumId(condominiumId, membership.name()))
                .orElseGet(ArrayList::new);
    }

    public void acceptJoinRequest(String adminId, JoinRequest request, String unitId) {
        condominiumRepository.acceptJoinRequest(request.userId(), request.condominiumId(),
                unitId == null ? request.suggestedUnitId() : unitId);
    }

    public void rejectJoinRequest(String adminId, String userId, String condominiumId) {
        condominiumRepository.rejectJoinRequest(userId, condominiumId);
    }

    public void modifyLoanAccess(String adminId, String userId, String condominiumId, boolean allowed) {
        if (adminId.equals(userId)) throw new UnauthorizedException("Only the condominium admin can do this", R.string.error_no_admin);
        condominiumRepository.modifyLoanAccess(userId, condominiumId, allowed);
    }

    public List<Unit> getAllUnits(String condominiumId) {
        if (condominiumId == null) return new ArrayList<>();
        return condominiumRepository.getAllUnitsByCondominiumId(condominiumId);
    }

    public void createUnit(String condominiumId, String name) {
        if (name.trim().isEmpty()) throw new BadRequestException("Apartment is empty", R.string.error_campo_obligatorio);
        condominiumRepository.createUnit(condominiumId, name.trim());
    }

    public void modifyUnit(String condominiumId, Unit unit, String name) {
        if (name.trim().isEmpty()) throw new BadRequestException("Apartment is empty", R.string.error_campo_obligatorio);
        condominiumRepository.modifyUnit(condominiumId, unit.id(), name.trim());
    }

    public void deleteUnit(String condominiumId, Unit unit) {
        condominiumRepository.deleteUnit(condominiumId, unit.id());
    }

    private Optional<CondominiumMembership> findMembership(String condominiumId) {
        if (condominiumId == null) return Optional.empty();
        return condominiumRepository.getAllMemberships().stream()
                .filter(membership -> membership.condominiumId().equals(condominiumId))
                .findFirst();
    }

    private void ensureAdmin(String adminId, String condominiumId) {
        if (!isAdmin(adminId, condominiumId)) throw new UnauthorizedException("Only the condominium admin can do this", R.string.error_no_admin);
    }
}
