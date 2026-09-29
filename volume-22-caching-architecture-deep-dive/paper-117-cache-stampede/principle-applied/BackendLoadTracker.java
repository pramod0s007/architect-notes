import java.util.HashMap;
import java.util.Map;

/**
 * Given a map of key -> expiry tick, computes how many keys expire at
 * each tick, and the single busiest tick's count -- the peak number of
 * simultaneous backend recompute calls the cache would trigger if every
 * expired key is re-fetched from the backend the instant it expires.
 */
public class BackendLoadTracker {
    public Map<Integer, Integer> callsPerTick(Map<Integer, Integer> expiryByKey) {
        Map<Integer, Integer> callsPerTick = new HashMap<>();
        for (int expiryTick : expiryByKey.values()) {
            callsPerTick.merge(expiryTick, 1, Integer::sum);
        }
        return callsPerTick;
    }

    public int peakCallsAtAnyTick(Map<Integer, Integer> callsPerTick) {
        int peak = 0;
        for (int calls : callsPerTick.values()) {
            peak = Math.max(peak, calls);
        }
        return peak;
    }
}
