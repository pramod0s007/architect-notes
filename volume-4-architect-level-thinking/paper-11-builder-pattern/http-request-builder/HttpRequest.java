import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Immutable HTTP request built via {@link HttpRequestBuilder}.
 * See: volume-4/.../paper-11-builder-pattern
 */
public final class HttpRequest {

    private final String url;
    private final String method;
    private final int timeoutMillis;
    private final Map<String, String> headers;
    private final String body;

    HttpRequest(
            String url,
            String method,
            int timeoutMillis,
            Map<String, String> headers,
            String body) {
        this.url = url;
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body;
    }

    public String url() {
        return url;
    }

    public String method() {
        return method;
    }

    public int timeoutMillis() {
        return timeoutMillis;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    @Override
    public String toString() {
        return method + " " + url + " timeout=" + timeoutMillis + "ms headers=" + headers.size()
                + (body == null || body.isEmpty() ? "" : " body=" + body);
    }
}
