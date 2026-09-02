import java.util.HashMap;
import java.util.Map;

public class Main {
    // A pooled worker thread never dies between requests -- it's reused for
    // the next one, and the one after that. A ThreadLocal's value survives
    // exactly as long as the thread does, which for a pooled thread means
    // "indefinitely," unless something explicitly clears it.
    static final ThreadLocal<Map<String, String>> CACHE = ThreadLocal.withInitial(HashMap::new);

    static void processRequest(int requestId) {
        // Intended as request-scoped data -- nothing ever removes it once
        // the request is done.
        CACHE.get().put("req-" + requestId, "data-for-request-" + requestId);
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
        System.out.println(residualEntries == requestCount
            ? "CONFIRMED FAILURE: every one of the " + requestCount + " requests' 'request-scoped' data is "
              + "still sitting in the thread-local map, because nothing ever removed it and the thread "
              + "that owns it never died to release it naturally. On a real pooled thread that goes on to "
              + "handle thousands more requests over its lifetime, this map grows without bound for as "
              + "long as the thread lives."
            : "Unexpected -- investigate.");
    }
}
