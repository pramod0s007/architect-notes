/**
 * Demonstrates composing multiple decorators around a base HTTP client.
 *
 * The decorator stack (outermost first):
 *   LoggingDecorator
 *     -> CachingDecorator
 *       -> RetryDecorator
 *         -> CircuitBreakerDecorator
 *           -> SimpleHttpClient (base)
 *
 * Each concern is a separate class; they compose without any one decorator
 * knowing about the others.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        // ── Build the decorator stack ──────────────────────────────────────
        SimpleHttpClient           base    = new SimpleHttpClient();
        CircuitBreakerDecorator    cb      = new CircuitBreakerDecorator(base, 3, 100);
        RetryDecorator             retry   = new RetryDecorator(cb, 2, 50);
        CachingDecorator           cache   = new CachingDecorator(retry, 2_000, 10);
        LoggingDecorator           client  = new LoggingDecorator(cache);

        // ── 1. Normal GET — first call hits network, second hits cache ─────
        System.out.println("=== 1. Caching Demo: Two GETs for the same URL ===");
        Response r1 = client.get("https://api.example.com/products/42");
        System.out.println("  Response: " + r1.getStatusCode() + " — " + r1.getBody());

        Response r2 = client.get("https://api.example.com/products/42");  // cache hit
        System.out.println("  Response: " + r2.getStatusCode() + " (from cache)");

        // ── 2. POST — not cached, always goes to network ──────────────────
        System.out.println("\n=== 2. POST (not cached) ===");
        Response postResp = client.post(
                "https://api.example.com/orders",
                "{\"productId\":42,\"qty\":1}");
        System.out.println("  Response: " + postResp.getStatusCode() + " — " + postResp.getBody());

        // ── 3. Retry on 5xx ───────────────────────────────────────────────
        // /fail always returns 503; RetryDecorator will attempt up to 3 times
        // then CircuitBreaker will trip
        System.out.println("\n=== 3. Retry Exhaustion + Circuit Breaker Trip ===");
        Response failResp = client.get("https://api.example.com/service/fail");
        System.out.println("  Final response: " + failResp.getStatusCode() + " — " + failResp.getBody());
        System.out.println("  Circuit state: " + cb.getState());

        // ── 4. Circuit breaker open — requests short-circuit ─────────────
        System.out.println("\n=== 4. Circuit Breaker OPEN — all requests blocked ===");
        Response blocked = client.get("https://api.example.com/products/99");
        System.out.println("  Response: " + blocked.getStatusCode() + " — " + blocked.getBody());

        // ── 5. Wait for circuit to move to HALF-OPEN, probe succeeds ─────
        System.out.println("\n=== 5. Circuit Breaker Recovery (wait 150ms) ===");
        Thread.sleep(150);
        Response probe = client.get("https://api.example.com/products/10");
        System.out.println("  Response: " + probe.getStatusCode());
        System.out.println("  Circuit state: " + cb.getState());

        // ── 6. Different URL — demonstrates cache size ────────────────────
        System.out.println("\n=== 6. Cache Size After Multiple URLs ===");
        client.get("https://api.example.com/categories");
        client.get("https://api.example.com/brands");
        System.out.println("  Cache entries: " + cache.cacheSize());
    }
}
