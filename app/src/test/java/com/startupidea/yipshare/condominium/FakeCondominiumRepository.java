package com.startupidea.yipshare.condominium;

import java.util.ArrayList;
import java.util.List;

public class FakeCondominiumRepository implements CondominiumRepository {

    public final List<CondominiumMembership> memberships = new ArrayList<>();
    public CondominiumPayload sent;
    public String joinedCode;
    public String joinedUnit;
    public String acceptedUnitId;
    public String loanAccessUserId;
    public String createdUnit;

    @Override
    public void createCondominiumRequest(CondominiumPayload payload) {
        sent = payload;
    }

    @Override
    public void createPendingMember(String shortCode, String unit) {
        joinedCode = shortCode;
        joinedUnit = unit;
    }

    @Override
    public List<CondominiumMembership> getAllMemberships() {
        return memberships;
    }

    @Override
    public List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId) {
        return new ArrayList<>();
    }

    @Override
    public List<JoinRequest> getAllJoinRequestsByCondominiumId(String condominiumId, String condominiumName) {
        List<JoinRequest> requests = new ArrayList<>();
        requests.add(new JoinRequest("maria", "maria", "María", null, condominiumId, condominiumName, "5", null, 0));
        return requests;
    }

    @Override
    public void acceptJoinRequest(String userId, String condominiumId, String unitId) {
        acceptedUnitId = unitId;
    }

    @Override
    public void rejectJoinRequest(String userId, String condominiumId) {
    }

    @Override
    public void modifyLoanAccess(String userId, String condominiumId, boolean allowed) {
        loanAccessUserId = userId;
    }

    @Override
    public List<Unit> getAllUnitsByCondominiumId(String condominiumId) {
        return new ArrayList<>();
    }

    @Override
    public void createUnit(String condominiumId, String name) {
        createdUnit = name;
    }

    @Override
    public void modifyUnit(String condominiumId, String unitId, String name) {
    }

    @Override
    public void deleteUnit(String condominiumId, String unitId) {
    }
}
