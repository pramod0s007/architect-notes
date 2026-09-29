import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * A TTL cache that coalesces concurrent loads for the same key: if the entry
 * is missing or expired and multiple threads ask for it at once, only the
 * first one actually calls the (slow) backing store. Every other thread
 * waits on that same in-flight load and reuses its result instead of
 * starting a redundant load of its own.
 *
 * Unlike a design with two separate maps (one for cached values, one for
 * in-flight loads), this uses a SINGLE map of key -> CompletableFuture, and
 * decides "reuse the existing future / start a new load" inside one atomic
 * ConcurrentHashMap.compute() call. That atomicity is what actually
 * matters: checking "is this entry expired?" and "assign a new loading
 * future if so" have to happen as one indivisible step, or a thread that's
 * delayed by scheduling jitter between those two checks can trigger a
 * second, redundant load.
 */
public class SingleFlightCache {
    private static class Entry {
        final String value;
        final long expiresAtMs;

        Entry(String value, long expiresAtMs) {
            this.value = value;
            this.expiresAtMs = expiresAtMs;
        }
    }

    private final ConcurrentHashMap<String, CompletableFuture<Entry>> state = new ConcurrentHashMap<>();
    private final BackingStore backingStore;
    private final long ttlMs;

    public SingleFlightCache(BackingStore backingStore, long ttlMs) {
        this.backingStore = backingStore;
        this.ttlMs = ttlMs;
    }

    public String get(String key) {
        long now = System.currentTimeMillis();

        CompletableFuture<Entry> future = state.compute(key, (k, existing) -> {
            if (existing != null && existing.isDone() && !existing.isCompletedExceptionally()) {
                Entry cached = existing.getNow(null);
                if (cached != null && cached.expiresAtMs > now) {
                    return existing; // still valid: reuse it, no new load
                }
            } else if (existing != null && !existing.isDone()) {
                return existing; // a load for this key is already in flight: reuse it
            }
            // Missing, expired, or previously failed: load now, while still holding
            // the per-key lock that compute() guarantees -- this is what makes the
            // "check expiry, then load" sequence atomic instead of racy.
            CompletableFuture<Entry> fresh = new CompletableFuture<>();
            try {
                String value = backingStore.load(k);
                fresh.complete(new Entry(value, System.currentTimeMillis() + ttlMs));
            } catch (RuntimeException e) {
                fresh.completeExceptionally(e);
            }
            return fresh;
        });

        try {
            return future.get().value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
