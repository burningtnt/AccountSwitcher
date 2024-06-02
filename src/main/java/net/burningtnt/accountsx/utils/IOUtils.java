package net.burningtnt.accountsx.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void openBrowser(String title, String url, Callback<String> callback) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText(title);
        shell.setSize(800, 600);

        final Browser browser = new Browser(shell, SWT.FILL);
        browser.setBounds(5, 5, 780, 560);
        browser.setUrl(url);
        browser.addLocationListener(new LocationAdapter() {
            @Override
            public void changed(LocationEvent event) {
                if (!url.equals(event.location)) {
                    if (callback.execute(event.location))
                        shell.close();
                }
            }
        });
        shell.open();
        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();
        display.dispose();
    }

    public static JsonObject postRequest(HttpUriRequest request) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            try (Reader reader = IOUtils.readResponse(httpClient.execute(request))) {
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

    public static Reader readResponse(HttpResponse response) throws IOException {
        Header encoding = response.getEntity().getContentEncoding();
        if (encoding == null) {
            return new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
        }

        return new InputStreamReader(response.getEntity().getContent(), encoding.getValue());
    }

    public interface Callback<T> {
        boolean execute(T data);
    }
}
