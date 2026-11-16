package com.startupidea.yipshare.api;

import android.content.Context;

import com.startupidea.yipshare.error.FileTooLarge;
import com.startupidea.yipshare.error.ServerError;
import com.startupidea.yipshare.error.ServerUnavailable;
import com.startupidea.yipshare.user.SessionRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

//important change this url if we're going to use the api
    public static final String BASE_URL = "http://localhost:4600";

    private static final String JSON_TYPE = "application/json";
    private static final int TIMEOUT_MS = 15000;
    private static final int BUFFER_SIZE = 8192;
    private static final int PAYLOAD_TOO_LARGE = 413;

    private final SessionRepository sessionRepository;

    public ApiClient(Context context) {
        this.sessionRepository = new SessionRepository(context);
    }

    public JSONObject getObject(String path) {
        return toObject(send("GET", path, null, null));
    }

    public JSONArray getArray(String path) {
        try {
            return new JSONArray(send("GET", path, null, null));
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    public JSONObject post(String path, JSONObject body) {
        return toObject(send("POST", path, JSON_TYPE, toBytes(body)));
    }

    public JSONObject patch(String path, JSONObject body) {
        return toObject(send("PATCH", path, JSON_TYPE, toBytes(body)));
    }

    public void put(String path, JSONObject body) {
        send("PUT", path, JSON_TYPE, toBytes(body));
    }

    public void delete(String path) {
        send("DELETE", path, null, null);
    }

    public String upload(InputStream input, String contentType) {
        try {
            return toObject(send("POST", "/uploads", contentType, readAll(input))).getString("path");
        } catch (ServerError e) {
            if (e.getStatus() == PAYLOAD_TOO_LARGE) throw new FileTooLarge();
            throw e;
        } catch (IOException | JSONException e) {
            throw new ServerUnavailable();
        }
    }

    public void download(String path, File target) {
        HttpURLConnection connection = open("GET", path, null);
        try (InputStream input = connection.getInputStream(); OutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        } catch (IOException e) {
            target.delete();
            throw new ServerUnavailable();
        } finally {
            connection.disconnect();
        }
    }

    public static String text(JSONObject json, String key) {
        return json.isNull(key) ? null : json.optString(key);
    }

    private String send(String method, String path, String contentType, byte[] body) {
        HttpURLConnection connection = open(method, path, contentType);
        try {
            if (body != null) {
                connection.setDoOutput(true);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response = stream == null ? "" : new String(readAll(stream), StandardCharsets.UTF_8);
            if (status >= 400) throw new ServerError(status, messageOf(response));
            return response;
        } catch (IOException e) {
            throw new ServerUnavailable();
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection open(String method, String path, String contentType) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            if (contentType != null) connection.setRequestProperty("Content-Type", contentType);
            String token = sessionRepository.getToken();
            if (token != null) connection.setRequestProperty("Authorization", "Bearer " + token);
            return connection;
        } catch (IOException e) {
            throw new ServerUnavailable();
        }
    }

    private static JSONObject toObject(String response) {
        try {
            return response.isEmpty() ? new JSONObject() : new JSONObject(response);
        } catch (JSONException e) {
            throw new ServerUnavailable();
        }
    }

    private static byte[] toBytes(JSONObject body) {
        return (body == null ? new JSONObject() : body).toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String messageOf(String response) {
        try {
            return new JSONObject(response).optString("message", response);
        } catch (JSONException e) {
            return response;
        }
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        return output.toByteArray();
    }
}
