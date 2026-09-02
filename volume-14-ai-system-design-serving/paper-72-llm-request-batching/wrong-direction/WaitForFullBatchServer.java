import java.util.ArrayList;
import java.util.List;

// The overcorrection: batch aggressively, and NEVER trigger a batch until it's
// completely full -- no timeout fallback. Under heavy, steady traffic this is
// indistinguishable from DynamicBatchServer. Under real, bursty or low traffic,
// it has no way to ever process a partial batch.
class WaitForFullBatchServer {
    private final int requiredBatchSize;

    WaitForFullBatchServer(int requiredBatchSize) {
        this.requiredBatchSize = requiredBatchSize;
    }

    // Returns only the requests that were part of a FULL batch -- anything left
    // over, however long it's been waiting, is simply never processed.
    List<String> processableRequests(List<String> arrivedRequests) {
        int fullBatches = arrivedRequests.size() / requiredBatchSize;
        int processableCount = fullBatches * requiredBatchSize;
        return new ArrayList<>(arrivedRequests.subList(0, processableCount));
    }
}
