import java.util.ArrayList;
import java.util.List;

// Triggers a batch on whichever comes first: the batch reaches maxBatchSize, OR
// the oldest waiting request has been waiting maxWaitMs. Under heavy, steady
// traffic, batches fill by SIZE almost every time (the timeout rarely matters).
// Under quiet traffic, the timeout guarantees a request is never stuck waiting
// forever for arrivals that may not come.
class DynamicBatchServer {
    static final int FIXED_OVERHEAD_MS = 50;
    static final int PER_ITEM_COST_MS = 5;

    private final int maxBatchSize;
    private final long maxWaitMs;

    DynamicBatchServer(int maxBatchSize, long maxWaitMs) {
        this.maxBatchSize = maxBatchSize;
        this.maxWaitMs = maxWaitMs;
    }

    // requestsInArrivalOrder must already be sorted by arrivalTimeMs.
    // observationEndMs simulates the point in time we're checking status at --
    // in a real server this would just be "now," continuously, via a background
    // timer that fires even with no new arrivals.
    List<List<Request>> formBatches(List<Request> requestsInArrivalOrder, long observationEndMs) {
        List<List<Request>> batches = new ArrayList<>();
        List<Request> current = new ArrayList<>();
        long batchStartMs = -1;

        for (Request r : requestsInArrivalOrder) {
            if (current.isEmpty()) {
                batchStartMs = r.arrivalTimeMs;
            }
            current.add(r);
            if (current.size() == maxBatchSize) {
                batches.add(current);
                current = new ArrayList<>();
            } else if (r.arrivalTimeMs - batchStartMs >= maxWaitMs) {
                batches.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty() && observationEndMs - batchStartMs >= maxWaitMs) {
            batches.add(current);
        }
        return batches;
    }

    long totalProcessingTimeMs(List<List<Request>> batches) {
        long total = 0;
        for (List<Request> batch : batches) {
            total += FIXED_OVERHEAD_MS + (long) batch.size() * PER_ITEM_COST_MS;
        }
        return total;
    }
}
