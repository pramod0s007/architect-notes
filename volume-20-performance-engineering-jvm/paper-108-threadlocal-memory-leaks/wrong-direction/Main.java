import java.util.HashMap;
import java.util.Map;

public class Main {
    static final ThreadLocal<Map<String, String>> CACHE = ThreadLocal.withInitial(HashMap::new);
    static int correctTierReads = 0;
    static int degradedFallbacks = 0;

    // Step 1: compute something real work needs later in the SAME request,
    // and cache it.
    static void computeAndCacheTier(int requestId) {
        CACHE.get().put("tier", "premium-tier-for-request-" + requestId);
    }

    // A shared utility, called partway through request handling for a
    // completely different reason (audit logging) -- written defensively
    // to "avoid leaking anything," it clears the thread-local unconditionally,
    // with no awareness that THIS request's own later code still needs it.
    static void auditLogWithDefensiveCleanup() {
        CACHE.remove();
    }

    // Step 2: read back what step 1 computed, later in the same request.
    static String readCachedTier() {
        String tier = CACHE.get().get("tier");
        if (tier != null) {
            correctTierReads++;
            return tier;
        }
        degradedFallbacks++;
        return "default-tier (fallback -- cached value was missing)";
    }

    static void processRequest(int requestId) {
        computeAndCacheTier(requestId);
        auditLogWithDefensiveCleanup(); // overcorrection: wipes the cache mid-request
        readCachedTier();
    }

    public static void main(String[] args) {
        int requestCount = 5;
        for (int i = 1; i <= requestCount; i++) {
            processRequest(i);
        }

        System.out.println("Processed " + requestCount + " requests.");
        System.out.println("Requests where step 2 correctly read back step 1's cached value: " + correctTierReads);
        System.out.println("Requests where step 2 fell back to a degraded default instead: " + degradedFallbacks);

        System.out.println();
        System.out.println(degradedFallbacks == requestCount
            ? "CONFIRMED FAILURE: there is no memory leak anymore -- the cache never accumulates anything "
              + "across requests, exactly like the fix. But every single request's own later step needed "
              + "the value its own earlier step computed, and found it gone, because a shared utility "
              + "called partway through the SAME request cleared the thread-local defensively, with no "
              + "way to know that clearing it was safe for its own caller but not for the rest of the "
              + "request still in flight. The leak was fixed by breaking every request instead."
            : "Unexpected -- investigate.");
    }
}
