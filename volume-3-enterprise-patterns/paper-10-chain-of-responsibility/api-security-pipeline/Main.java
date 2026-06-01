import java.util.Map;
import java.util.Set;

/**
 * Run: javac *.java && java Main
 *
 * Demonstrates multiple pipeline scenarios: blocked IP, invalid token,
 * unauthorized user, rate-limit exceeded, empty body, and a fully valid request.
 */
public final class Main {

    public static void main(String[] args) {

        // ── Pipeline configuration ────────────────────────────────────────────

        Set<String> blockedIps = Set.of("10.0.0.99", "192.168.1.200");

        Map<String, Set<String>> permissions = Map.of(
                "alice",   Set.of("/api/products", "/api/orders"),
                "bob",     Set.of("/api/products"),
                "admin-1", Set.of("/api/")             // admin: all endpoints
        );

        RateLimitHandler rateLimiter = new RateLimitHandler(2); // low limit for demo

        SecurityHandler pipeline = new PipelineBuilder()
                .add(new IpBlocklistHandler(blockedIps))
                .add(new AuthenticationHandler())
                .add(new AuthorizationHandler(permissions))
                .add(rateLimiter)
                .add(new RequestValidationHandler())
                .build();

        // ── Scenarios ─────────────────────────────────────────────────────────

        scenario("1. Blocked IP", pipeline,
                new ApiRequest("10.0.0.99", "Bearer validtoken12345", "alice",
                        "/api/products", "GET", ""));

        scenario("2. Invalid token", pipeline,
                new ApiRequest("10.0.0.1", "bad-token", "alice",
                        "/api/products", "GET", ""));

        scenario("3. Unauthorized endpoint", pipeline,
                new ApiRequest("10.0.0.1", "Bearer validtoken12345", "bob",
                        "/api/orders/123", "GET", ""));

        scenario("4a. Valid request (alice, first call)", pipeline,
                new ApiRequest("10.0.0.2", "Bearer validtoken12345", "alice",
                        "/api/products", "POST", "{\"name\":\"Widget\"}"));

        scenario("4b. Valid request (alice, second call)", pipeline,
                new ApiRequest("10.0.0.2", "Bearer validtoken12345", "alice",
                        "/api/products", "GET", ""));

        scenario("5. Rate limit exceeded (alice, third call)", pipeline,
                new ApiRequest("10.0.0.2", "Bearer validtoken12345", "alice",
                        "/api/products", "GET", ""));

        scenario("6. Missing body on POST", pipeline,
                new ApiRequest("10.0.0.3", "Bearer admintoken99999", "admin-1",
                        "/api/products", "POST", ""));
    }

    private static void scenario(String label, SecurityHandler pipeline, ApiRequest request) {
        System.out.println("\n─── " + label + " ───");
        System.out.println("  Request: " + request);
        ApiResponse response = pipeline.handle(request);
        System.out.println("  Result:  " + response);
    }
}
