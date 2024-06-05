package net.burningtnt.accountsx.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.burningtnt.accountsx.accounts.AccountUUID;
import net.minecraft.util.Util;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class IOUtils {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new AccountUUID.UUIDTypeAdapter())
            .setPrettyPrinting()
            .create();

    public static void openBrowser(String url) {
        Util.getOperatingSystem().open(url);
    }

    public static JsonObject postRequest(HttpUriRequest request) throws IOException {
        return postRequest(request, false);
    }

    public static JsonObject postRequest(HttpUriRequest request, boolean ignoreHttpStatus) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            try (Reader reader = IOUtils.readResponse(httpClient.execute(request), ignoreHttpStatus)) {
                return IOUtils.GSON.fromJson(reader, JsonObject.class);
            }
        }
    }

    public static JsonObject postRequest(String url, JsonElement json) throws IOException {
        return postRequest(RequestBuilder.post(url)
                .addHeader("Content-Type", "application/json")
                .setEntity(new StringEntity(IOUtils.GSON.toJson(json)))
                .build());
    }

    public static Reader readResponse(HttpResponse response, boolean ignoreHttpStatus) throws IOException {
        if (!ignoreHttpStatus) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode / 100 != 2) {
                throw new IOException("HTTP " + statusCode + ": " + response.getStatusLine().getReasonPhrase());
            }
        }

        Header encoding = response.getEntity().getContentEncoding();
        if (encoding == null) {
            return new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
        }

        return new InputStreamReader(response.getEntity().getContent(), encoding.getValue());
    }

    public static String withQuery(String baseUrl, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(baseUrl);
        boolean first = true;

        for (Map.Entry<String, String> param : params.entrySet()) {
            if (param.getValue() == null)
                continue;
            if (first) {
                if (!baseUrl.isEmpty()) {
                    sb.append('?');
                }
                first = false;
            } else {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
