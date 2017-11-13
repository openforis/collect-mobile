package org.openforis.collect.android.util;

import android.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnectionHelper {

    private String address;
    private String username;
    private String password;

    public HttpConnectionHelper(String address, String username, String password) {
        this.address = address;
        this.username = username;
        this.password = password;
    }

    public JsonObject getJson() throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();

            addBasicAuthorization(conn, username, password);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            JsonObject jsonObject = new JsonParser().parse(new InputStreamReader(in, "UTF-8")).getAsJsonObject();
            return jsonObject;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void addBasicAuthorization(HttpURLConnection connection, String username, String password) {
        String encodedUsernamePassword = Base64.encodeToString((username+ ":" + password).getBytes(), Base64.DEFAULT);
        connection.setRequestProperty("Authorization", "Basic " + encodedUsernamePassword);
    }

    public interface JsonResponseProcessor {
        void success(JsonObject jsonObject);
        void error(String error);
    }

    public interface ResponseProcessor {
        void success(String response);
        void error(String error);
    }

}
