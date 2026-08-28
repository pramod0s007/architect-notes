import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Tracks aggregate numbers over time. Can tell you THAT something is
// slow. Has no structural way to tell you WHERE in a multi-service call
// chain the time actually went -- that information was never captured.
class MetricsOnlyObserver {
    private final List<Long> requestLatenciesMs = new ArrayList<>();

    void recordRequest(long totalLatencyMs) {
        requestLatenciesMs.add(totalLatencyMs);
    }

    long p99LatencyMs() {
        List<Long> sorted = new ArrayList<>(requestLatenciesMs);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.99 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }
}
