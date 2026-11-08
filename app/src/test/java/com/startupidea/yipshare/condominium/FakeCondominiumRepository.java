package com.startupidea.yipshare.condominium;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** In-memory condominium data for service tests; fields are public so each test sets up what it needs. */
public class FakeCondominiumRepository implements CondominiumRepository {

    public final Set<String> admins = new HashSet<>();          // "userId@condominiumId"
    public final Set<String> members = new HashSet<>();         // "userId@condominiumId"
    public final Set<String> withoutLoanAccess = new HashSet<>(); // "userId@condominiumId"
    public final Set<String> joinRequests = new HashSet<>();    // "userId@condominiumId"
    public final List<CondominiumMembership> memberships = new ArrayList<>();
    public String approvedCode;
    public CondominiumPayload sent;
    public String sentUserId;
    public String joinedUnit;
    public int demoCalls;

    public static String key(String userId, String condominiumId) {
        return userId + "@" + condominiumId;
    }

    @Override
    public void createCondominiumRequest(String userId, CondominiumPayload payload) {
        sentUserId = userId;
        sent = payload;
    }

    @Override
    public String getApprovedCondominiumIdByCode(String shortCode) {
        return shortCode.equals(approvedCode) ? "condo" : null;
    }

    @Override
    public boolean existsMembership(String userId, String condominiumId) {
        return members.contains(key(userId, condominiumId)) || joinRequests.contains(key(userId, condominiumId));
    }

    @Override
    public void createPendingMember(String userId, String condominiumId, String building, String apartment) {
        joinedUnit = building + "|" + apartment;
    }

    @Override
    public List<CondominiumMembership> getAllMembershipsByUserId(String userId) {
        return memberships;
    }

    @Override
    public boolean existsMembershipByUserId(String userId) {
        return !memberships.isEmpty();
    }

    @Override
    public boolean isCondominiumAdmin(String userId, String condominiumId) {
        return admins.contains(key(userId, condominiumId));
    }

    @Override
    public List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId) {
        return new ArrayList<>();
    }

    @Override
    public List<JoinRequest> getAllJoinRequestsByAdminId(String adminId, String condominiumId) {
        return new ArrayList<>();
    }

    @Override
    public boolean existsJoinRequest(String userId, String condominiumId) {
        return joinRequests.contains(key(userId, condominiumId));
    }

    @Override
    public void acceptJoinRequest(String userId, String condominiumId) {
        joinRequests.remove(key(userId, condominiumId));
        members.add(key(userId, condominiumId));
    }

    @Override
    public void rejectJoinRequest(String userId, String condominiumId) {
        joinRequests.remove(key(userId, condominiumId));
    }

    @Override
    public boolean canRequestLoans(String userId, String condominiumId) {
        return members.contains(key(userId, condominiumId)) && !withoutLoanAccess.contains(key(userId, condominiumId));
    }

    @Override
    public void modifyLoanAccess(String userId, String condominiumId, boolean allowed) {
        if (allowed) withoutLoanAccess.remove(key(userId, condominiumId));
        else withoutLoanAccess.add(key(userId, condominiumId));
    }

    @Override
    public void createDemoCondominiums(String userId) {
        demoCalls++;
        memberships.add(new CondominiumMembership("demo", "Demo", true, 0));
    }
}
