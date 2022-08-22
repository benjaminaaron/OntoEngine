package de.benjaminaaron.ontoengine.suggestion.job.task.base;

import okhttp3.*;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

// This is to fix a bug in ApiConnection (not sure yet if only in my setup or in library) causing no request to work, see line 40 and 41 here
// I raised an issue about this: https://github.com/Wikidata/Wikidata-Toolkit/issues/600
// I tried with the minimum amount of @Overrides possible
public class FixedBasicApiConnection extends BasicApiConnection {

    private OkHttpClient client;

    public FixedBasicApiConnection(String apiBaseUrl) {
        super(apiBaseUrl);
    }

    public static FixedBasicApiConnection getWikidataApiConnection() {
        return new FixedBasicApiConnection("https://www.wikidata.org/w/api.php");
    }

    @Override
    public InputStream sendRequest(String requestMethod, Map<String, String> parameters) throws IOException {
        String queryString = _getQueryString(parameters);
        Request request;
        if ("GET".equalsIgnoreCase(requestMethod)) {
            request = (new okhttp3.Request.Builder()).url(getApiBaseUrl() + "?" + queryString).build();
        } else {
            if (!"POST".equalsIgnoreCase(requestMethod)) {
                throw new IllegalArgumentException("Expected the requestMethod to be either GET or POST, but got " + requestMethod);
            }
            // here is the one fix that was needed to make it work, the two parameters going into RequestBody.create() have to be swapped
            request = (new okhttp3.Request.Builder()).url(getApiBaseUrl()).post(RequestBody.create(URLENCODED_MEDIA_TYPE, queryString)).build();
        }
        if (client == null) {
            buildClient();
        }
        Response response = client.newCall(request).execute();
        return ((ResponseBody) Objects.requireNonNull(response.body())).byteStream();
    }

    // @Override
    String _getQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        try {
            boolean first = true;
            Iterator var4 = params.entrySet().iterator();
            while (var4.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) var4.next();
                if (first) {
                    first = false;
                } else {
                    builder.append("&");
                }
                builder.append(URLEncoder.encode((String) entry.getKey(), "UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode((String) entry.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException var6) {
            throw new RuntimeException("Your Java version does not support UTF-8 encoding.");
        }
        return builder.toString();
    }

    // @Override
    private void buildClient() {
        OkHttpClient.Builder builder = getClientBuilder();
        if (this.connectTimeout >= 0) {
            builder.connectTimeout((long) this.connectTimeout, TimeUnit.MILLISECONDS);
        }
        if (this.readTimeout >= 0) {
            builder.readTimeout((long) this.readTimeout, TimeUnit.MILLISECONDS);
        }
        client = builder.build();
    }

    @Override
    public void setConnectTimeout(int timeout) {
        super.setConnectTimeout(timeout);
        client = null;
    }

    @Override
    public void setReadTimeout(int timeout) {
        super.setReadTimeout(timeout);
        client = null;
    }
}
