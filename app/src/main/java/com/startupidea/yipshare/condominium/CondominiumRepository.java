package com.startupidea.yipshare.condominium;

import java.util.List;

public interface CondominiumRepository {

    void createCondominiumRequest(CondominiumPayload payload);

    void createPendingMember(String shortCode, String unit);

    List<CondominiumMembership> getAllMemberships();

    List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId);

    List<JoinRequest> getAllJoinRequestsByCondominiumId(String condominiumId, String condominiumName);

    void acceptJoinRequest(String userId, String condominiumId, String unitId);

    void rejectJoinRequest(String userId, String condominiumId);

    void modifyLoanAccess(String userId, String condominiumId, boolean allowed);

    List<Unit> getAllUnitsByCondominiumId(String condominiumId);

    void createUnit(String condominiumId, String name);

    void modifyUnit(String condominiumId, String unitId, String name);

    void deleteUnit(String condominiumId, String unitId);
}
