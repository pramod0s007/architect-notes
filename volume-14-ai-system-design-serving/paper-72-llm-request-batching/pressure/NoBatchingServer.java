import java.util.List;

// Processes one request at a time. FIXED_OVERHEAD_MS represents the real,
// mostly-fixed cost of one inference pass (kernel launch, weight streaming from
// memory) that a GPU pays whether it processes 1 request or 32 -- standing in
// for a real inference engine's per-call overhead, disclosed here as illustrative,
// not a claim about any specific model or hardware's actual numbers.
class NoBatchingServer {
    static final int FIXED_OVERHEAD_MS = 50;
    static final int PER_ITEM_COST_MS = 5;

    long totalProcessingTimeMs(List<String> requests) {
        long total = 0;
        for (String r : requests) {
            total += FIXED_OVERHEAD_MS + PER_ITEM_COST_MS;
        }
        return total;
    }
}
