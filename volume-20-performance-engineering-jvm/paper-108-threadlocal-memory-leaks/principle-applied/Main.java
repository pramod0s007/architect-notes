import java.util.HashMap;
import java.util.Map;

public class Main {
    static final ThreadLocal<Map<String, String>> CACHE = ThreadLocal.withInitial(HashMap::new);

    static void processRequest(int requestId) {
        CACHE.get().put("req-" + requestId, "data-for-request-" + requestId);
        // ... the rest of this request's real work happens here, using
        // the cache entry it just stored ...

        // Cleanup happens at the END of the request, once, unconditionally
        // -- returning the thread-local state to empty before this thread
        // is reused for the next, unrelated request.
        CACHE.remove();
    }

    public static void main(String[] args) {
        int requestCount = 5;
        for (int i = 1; i <= requestCount; i++) {
            processRequest(i);
        }

        int residualEntries = CACHE.get().size();
        System.out.println("Processed " + requestCount + " requests on the same pooled thread.");
        System.out.println("Entries still resident in the thread-local cache after all requests finished: " + residualEntries);

        System.out.println();
        System.out.println(residualEntries == 0
            ? "FIXED: nothing from any prior request is still resident. Each request's cache entry was "
              + "used for exactly that request and then explicitly removed, so the thread returns to the "
              + "pool in the same clean state it would have been in if it had never processed a request "
              + "at all. This holds true whether the pooled thread handles 5 requests or 5 million."
            : "Unexpected -- investigate.");
    }
}
