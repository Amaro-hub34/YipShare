package com.startupidea.yipshare.condominium;

import android.content.Context;
import android.net.Uri;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiCondominiumRepository implements CondominiumRepository {

    private static final String PDF_TYPE = "application/pdf";
    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;

    private final ApiClient apiClient;
    private final Context context;

    public ApiCondominiumRepository(ApiClient apiClient, Context context) {
        this.apiClient = apiClient;
        this.context = context;
    }

    @Override
    public void createCondominiumRequest(CondominiumPayload payload) {
        try {
            apiClient.post("/condominiums", new JSONObject()
                    .put("name", payload.getName())
                    .put("street", payload.getStreet())
                    .put("neighborhood", payload.getNeighborhood())
                    .put("postalCode", payload.getZipCode())
                    .put("municipality", payload.getMunicipality())
                    .put("city", payload.getCity())
                    .put("state", payload.getState())
                    .put("units", payload.getUnits())
                    .put("deed", uploadDocument(payload.getDeedUri()))
                    .put("minutes", uploadDocument(payload.getMinutesUri()))
                    .put("taxId", uploadDocument(payload.getTaxIdUri()))
                    .put("identification", uploadDocument(payload.getIdentificationUri())));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void createPendingMember(String shortCode, String unit) {
        try {
            apiClient.post("/condominiums/join", new JSONObject().put("code", shortCode).put("unit", unit));
        } catch (ServerError e) {
            if (e.getStatus() == NOT_FOUND) throw new CondominiumNotFound();
            if (e.getStatus() == CONFLICT) throw new AlreadyJoined();
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<CondominiumMembership> getAllMemberships() {
        try {
            JSONObject mine = apiClient.getObject("/condominiums/mine");
            Map<String, CondominiumMembership> memberships = new LinkedHashMap<>();
            addMemberships(memberships, mine.getJSONArray("registered"), true);
            addMemberships(memberships, mine.getJSONArray("joined"), false);
            return new ArrayList<>(memberships.values());
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<Neighbor> getAllNeighborsByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/condominiums/" + condominiumId + "/members");
            Map<String, Neighbor> neighbors = new LinkedHashMap<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                String userId = json.getString("userId");
                Neighbor known = neighbors.get(userId);
                String unit = known == null ? json.optString("unitName") : known.getUnit() + ", " + json.optString("unitName");
                neighbors.put(userId, new Neighbor(userId, json.optString("username"), unit,
                        ApiClient.text(json, "phoneNumber"), json.optInt("computedRating"),
                        json.optBoolean("isAdmin"), json.optBoolean("canRequestLoans", true)));
            }
            return new ArrayList<>(neighbors.values());
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<JoinRequest> getAllJoinRequestsByCondominiumId(String condominiumId, String condominiumName) {
        try {
            JSONArray array = apiClient.getArray("/condominiums/" + condominiumId + "/members/pending");
            List<JoinRequest> requests = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                String fullName = joinNonEmpty(ApiClient.text(json, "name"), ApiClient.text(json, "paternalSurname"),
                        ApiClient.text(json, "maternalSurname"));
                requests.add(new JoinRequest(json.getString("userId"), json.optString("username"), fullName,
                        ApiClient.text(json, "phoneNumber"), condominiumId, condominiumName, json.optString("unit"),
                        ApiClient.text(json, "suggestedUnitId"), json.optInt("computedRating")));
            }
            return requests;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void acceptJoinRequest(String userId, String condominiumId, String unitId) {
        try {
            JSONObject body = new JSONObject();
            if (unitId != null) body.put("unitId", unitId);
            apiClient.post(memberPath(condominiumId, userId) + "/approve", body);
        } catch (ServerError e) {
            if (e.getStatus() == NOT_FOUND) throw new JoinRequestNotFound();
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void rejectJoinRequest(String userId, String condominiumId) {
        try {
            apiClient.delete(memberPath(condominiumId, userId) + "/pending");
        } catch (ServerError e) {
            if (e.getStatus() == NOT_FOUND) throw new JoinRequestNotFound();
            throw e;
        }
    }

    @Override
    public void modifyLoanAccess(String userId, String condominiumId, boolean allowed) {
        try {
            apiClient.put(memberPath(condominiumId, userId) + "/loan-access", new JSONObject().put("allowed", allowed));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<Unit> getAllUnitsByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/condominiums/" + condominiumId + "/units");
            List<Unit> units = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                units.add(new Unit(json.getString("condominiumUnitId"), json.optString("name")));
            }
            return units;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void createUnit(String condominiumId, String name) {
        try {
            apiClient.post("/condominiums/" + condominiumId + "/units", new JSONObject().put("name", name));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void modifyUnit(String condominiumId, String unitId, String name) {
        try {
            apiClient.patch("/condominiums/" + condominiumId + "/units/" + unitId, new JSONObject().put("name", name));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void deleteUnit(String condominiumId, String unitId) {
        try {
            apiClient.delete("/condominiums/" + condominiumId + "/units/" + unitId);
        } catch (ServerError e) {
            if (e.getStatus() == CONFLICT) throw new UnitInUse();
            throw e;
        }
    }

    private String uploadDocument(String documentUri) {
        if (documentUri == null) return null;
        try (InputStream input = context.getContentResolver().openInputStream(Uri.parse(documentUri))) {
            if (input == null) throw new ServerUnavailable();
            return apiClient.upload(input, PDF_TYPE);
        } catch (IOException | SecurityException e) {
            throw new ServerUnavailable();
        }
    }

    private static void addMemberships(Map<String, CondominiumMembership> memberships, JSONArray array, boolean admin)
            throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            String condominiumId = json.getString("condominiumId");
            if (memberships.containsKey(condominiumId)) continue;
            memberships.put(condominiumId, new CondominiumMembership(
                    condominiumId, json.optString("name"), admin, json.optInt("pendingCount"), ApiClient.text(json, "code")));
        }
    }

    private static String memberPath(String condominiumId, String userId) {
        return "/condominiums/" + condominiumId + "/members/" + userId;
    }

    private static String joinNonEmpty(String... parts) {
        StringBuilder joined = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            if (joined.length() > 0) joined.append(' ');
            joined.append(part.trim());
        }
        return joined.toString();
    }
}
