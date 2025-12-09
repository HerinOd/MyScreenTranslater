package org.example;

import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiTranslator {

    //TODO: Change API_KEY
    private static final String API_KEY = " ";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String translate(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        String jsonBody = buildJson(text);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(API_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                return parseResponse(responseData);
            } else {
                return "Lỗi API: " + response.code();
            }
        } catch (IOException e) {
            return "Lỗi mạng: " + e.getMessage();
        }
    }

    private static String buildJson(String text) {
        JsonObject part = new JsonObject();
        part.addProperty("text", "Translate to Vietnamese. Output ONLY translation. Text: " + text);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject root = new JsonObject();
        root.add("contents", contents);

        return root.toString();
    }

    private static String parseResponse(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return root.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return "Lỗi đọc JSON.";
        }
    }
}
