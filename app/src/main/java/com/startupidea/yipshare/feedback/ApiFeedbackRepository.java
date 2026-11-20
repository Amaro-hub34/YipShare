package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.ConflictException;
import android.content.Context;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;
import com.startupidea.yipshare.utils.ImageFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiFeedbackRepository implements FeedbackRepository {

    private static final int RATING_SCALE = 10;
    private static final int CONFLICT = 409;

    private final ApiClient apiClient;
    private final Context context;

    public ApiFeedbackRepository(ApiClient apiClient, Context context) {
        this.apiClient = apiClient;
        this.context = context;
    }

    @Override
    public List<Review> getAllReviewsByUserId(String userId) {
        try {
            JSONArray array = apiClient.getArray("/reviews/users/" + userId);
            List<Review> reviews = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                reviews.add(new Review(json.optString("from"), json.optString("comment"), json.optInt("stars") * RATING_SCALE));
            }
            return reviews;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<Report> getAllReportsByUserId(String userId) {
        try {
            JSONArray array = apiClient.getArray("/reports/users/" + userId);
            List<Report> reports = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                reports.add(new Report(null, json.optString("text"),
                        json.optBoolean("resolved") ? ReportState.RESOLVED : ReportState.PENDING));
            }
            return reports;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void createReview(String loanId, int stars, String text) {
        try {
            apiClient.post("/reviews", new JSONObject().put("loanId", loanId).put("stars", stars).put("comment", text));
        } catch (ServerError e) {
            if (e.getStatus() == CONFLICT) throw new ConflictException("User already reviewed this loan", R.string.error_ya_calificaste);
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void createReport(String loanId, String text, List<String> imageUris) {
        try {
            JSONArray images = new JSONArray();
            for (String imageUri : imageUris) images.put(ImageFileUtil.upload(context, apiClient, imageUri));
            JSONObject body = new JSONObject().put("loanId", loanId).put("text", text).put("images", images);
            apiClient.post("/reports", body);
        } catch (ServerError e) {
            if (e.getStatus() == CONFLICT) throw new ConflictException("User already reported this loan", R.string.error_ya_reportado);
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<Review> getAllReviewsByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/condominiums/" + condominiumId + "/reviews");
            List<Review> reviews = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                String author = json.optString("from") + " → " + json.optString("to") + " · " + json.optString("article");
                reviews.add(new Review(author, json.optString("comment"), json.optInt("stars") * RATING_SCALE));
            }
            return reviews;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<Report> getAllReportsByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/condominiums/" + condominiumId + "/reports");
            List<Report> reports = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                String text = json.optString("reportedBy") + " → " + json.optString("reported") + ": " + json.optString("text");
                reports.add(new Report(json.getString("id"), text, ReportState.parse(json.getString("state"))));
            }
            return reports;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void modifyReportState(String condominiumId, String reportId, ReportState state) {
        try {
            apiClient.patch("/condominiums/" + condominiumId + "/reports/" + reportId,
                    new JSONObject().put("state", state.getValue()));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }
}
