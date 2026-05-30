import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpRequestBuilder {

    private String url;
    private String method = "GET";
    private int timeoutMillis = 30_000;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String body;

    public HttpRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public HttpRequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder timeout(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public HttpRequestBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpRequestBuilder body(String body) {
        this.body = body;
        return this;
    }

    public HttpRequest build() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("url is required");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalStateException("timeout must be positive");
        }
        if ("POST".equalsIgnoreCase(method) && (body == null || body.isBlank())) {
            throw new IllegalStateException("POST requires a body");
        }
        return new HttpRequest(url, method.toUpperCase(), timeoutMillis, headers, body);
    }
}
