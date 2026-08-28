import java.util.ArrayList;
import java.util.List;

// A single request's full path, broken down by which service spent how
// long. Answers "where did the time go" for THIS specific request --
// exactly what an aggregate metric structurally cannot.
class Trace {
    private final List<Span> spans = new ArrayList<>();

    void addSpan(String serviceName, long durationMs) {
        spans.add(new Span(serviceName, durationMs));
    }

    long totalDurationMs() {
        long total = 0;
        for (Span s : spans) {
            total += s.durationMs;
        }
        return total;
    }

    Span slowestSpan() {
        Span slowest = null;
        for (Span s : spans) {
            if (slowest == null || s.durationMs > slowest.durationMs) {
                slowest = s;
            }
        }
        return slowest;
    }
}
