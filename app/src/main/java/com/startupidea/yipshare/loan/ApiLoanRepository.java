package com.startupidea.yipshare.loan;

import android.content.Context;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;
import com.startupidea.yipshare.item.ItemNotAvailable;
import com.startupidea.yipshare.utils.ImageFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ApiLoanRepository implements LoanRepository {

    private static final int FORBIDDEN = 403;
    private static final int CONFLICT = 409;

    private final ApiClient apiClient;
    private final Context context;

    public ApiLoanRepository(ApiClient apiClient, Context context) {
        this.apiClient = apiClient;
        this.context = context;
    }

    @Override
    public List<Loan> getAllLoansByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/loans/me?condominiumId=" + condominiumId);
            List<Loan> loans = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) loans.add(readLoan(array.getJSONObject(i)));
            return loans;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public List<LoanRequest> getAllLoanRequestsByCondominiumId(String condominiumId) {
        try {
            JSONArray array = apiClient.getArray("/loans/me/lent?condominiumId=" + condominiumId);
            List<LoanRequest> requests = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                if (LoanStatus.parse(json.getString("state")) != LoanStatus.PENDING) continue;
                requests.add(new LoanRequest(
                        json.getString("loanId"),
                        json.optString("articleName"),
                        json.getString("otherUserId"),
                        json.optString("otherUsername"),
                        json.optInt("otherRating"),
                        json.optString("requestText")));
            }
            return requests;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void createLoanRequest(String articleId, String requestText, int days) {
        try {
            apiClient.post("/loans", new JSONObject()
                    .put("articleId", articleId)
                    .put("requestText", requestText)
                    .put("days", days));
        } catch (ServerError e) {
            if (e.getStatus() == FORBIDDEN) throw new LoanAccessRevoked();
            if (e.getStatus() == CONFLICT && e.messageContains("pending")) throw new AlreadyRequested();
            if (e.getStatus() == CONFLICT) throw new ItemNotAvailable();
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public void modifyLoanStatus(String loanId, LoanStatus answer) {
        try {
            apiClient.patch("/loans/me/" + loanId + (answer == LoanStatus.ACCEPTED ? "/accept" : "/reject"), null);
        } catch (ServerError e) {
            if (e.getStatus() == CONFLICT) throw new LoanRequestNotPending();
            throw e;
        }
    }

    @Override
    public String createCode(String loanId) {
        try {
            return apiClient.post("/loans/me/" + loanId + "/verification-code", null).getString("code");
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    @Override
    public Loan confirmCode(String loanId, String code) {
        try {
            return readLoan(apiClient.patch("/loans/me/" + loanId + "/confirm", new JSONObject().put("code", code)));
        } catch (ServerError e) {
            if (e.getStatus() == FORBIDDEN) throw new InvalidCode();
            if (e.getStatus() == CONFLICT) throw new LoanNotActive();
            throw e;
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    private Loan readLoan(JSONObject json) throws JSONException {
        return new Loan(
                json.getString("loanId"),
                json.optString("articleName"),
                json.optString("description"),
                ImageFileUtil.toLocalUri(context, apiClient, ApiClient.text(json, "image")),
                json.getString("otherUserId"),
                json.optString("otherUsername"),
                json.optBoolean("lentByMe"),
                toDate(ApiClient.text(json, "startedAt")),
                toDate(ApiClient.text(json, "dueAt")),
                LoanStatus.parse(json.getString("state")),
                ApiClient.text(json, "otherPhoneNumber"));
    }

    private static String toDate(String isoDate) {
        if (isoDate == null) return null;
        SimpleDateFormat server = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        server.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(server.parse(isoDate));
        } catch (ParseException e) {
            return null;
        }
    }
}
