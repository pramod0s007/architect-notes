import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Decorator that caches GET responses by URL with a simple TTL.
 *
 * Only GET requests are cached (POST requests are always forwarded). An entry
 * is evicted when its TTL expires. The cache is capped at {@code maxEntries}
 * via an LRU eviction policy.
 */
public class CachingDecorator implements HttpClient {

    private final HttpClient delegate;
    private final long ttlMs;
    private final int maxEntries;

    private final Map<String, CacheEntry> cache;

    public CachingDecorator(HttpClient delegate, long ttlMs, int maxEntries) {
        this.delegate   = delegate;
        this.ttlMs      = ttlMs;
        this.maxEntries = maxEntries;
        // Access-ordered LinkedHashMap for LRU eviction
        this.cache = new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > maxEntries;
            }
        };
    }

    @Override
    public Response get(String url) {
        synchronized (cache) {
            CacheEntry entry = cache.get(url);
            if (entry != null && !entry.isExpired()) {
                System.out.println("  [Cache] HIT  " + url);
                return entry.response;
            }
            if (entry != null) {
                System.out.println("  [Cache] EXPIRED " + url);
                cache.remove(url);
            }
        }

        System.out.println("  [Cache] MISS " + url);
        Response response = delegate.get(url);

        if (response.isSuccess()) {
            synchronized (cache) {
                cache.put(url, new CacheEntry(response, System.currentTimeMillis() + ttlMs));
            }
        }
        return response;
    }

    @Override
    public Response post(String url, String body) {
        // POST requests are never cached — always forwarded
        return delegate.post(url, body);
    }

    public int cacheSize() {
        synchronized (cache) { return cache.size(); }
    }

    // ── Internal entry type ───────────────────────────────────────────────

    private static class CacheEntry {
        final Response response;
        final long expiresAt;

        CacheEntry(Response response, long expiresAt) {
            this.response  = response;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
