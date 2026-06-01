import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulated HTTP client — the base (concrete component).
 *
 * Returns deterministic responses for demo purposes. URLs ending in
 * "/fail" return 503 so decorators can demonstrate retry / circuit-breaker.
 * Every third call to an /unstable URL returns 503 to simulate intermittent
 * failures.
 */
public class SimpleHttpClient implements HttpClient {

    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public Response get(String url) {
        int n = callCount.incrementAndGet();
        System.out.println("  [SimpleHttpClient] GET " + url + " (call #" + n + ")");

        if (url.contains("/fail")) {
            return new Response(503, "Service Unavailable");
        }
        if (url.contains("/unstable") && n % 3 != 0) {
            return new Response(503, "Temporary server error");
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Request-Id", "req-" + n);
        return new Response(200, "{\"url\":\"" + url + "\",\"call\":" + n + "}", headers);
    }

    @Override
    public Response post(String url, String body) {
        int n = callCount.incrementAndGet();
        System.out.println("  [SimpleHttpClient] POST " + url + " body=" + body + " (call #" + n + ")");

        if (url.contains("/fail")) {
            return new Response(500, "Internal Server Error");
        }

        return new Response(201, "{\"created\":true,\"echo\":" + body + "}");
    }
}
