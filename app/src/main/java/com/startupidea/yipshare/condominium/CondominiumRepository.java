package com.startupidea.yipshare.condominium;

import java.util.List;

public interface CondominiumRepository {

    void createCondominiumRequest(String userId, CondominiumPayload payload);

    String getApprovedCondominiumIdByCode(String shortCode);

    boolean existsMembership(String userId, String condominiumId);

    void createPendingMember(String userId, String condominiumId, String building, String apartment);

    List<CondominiumMembership> getAllMembershipsByUserId(String userId);

    boolean existsMembershipByUserId(String userId);

    boolean isCondominiumAdmin(String userId, String condominiumId);

    List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId);

    List<JoinRequest> getAllJoinRequestsByAdminId(String adminId, String condominiumId);

    boolean existsJoinRequest(String userId, String condominiumId);

    void acceptJoinRequest(String userId, String condominiumId);

    void rejectJoinRequest(String userId, String condominiumId);

    boolean canRequestLoans(String userId, String condominiumId);

    void modifyLoanAccess(String userId, String condominiumId, boolean allowed);

    void createDemoCondominiums(String userId);
}
