import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Three concurrent requests hit the same chain around the same time.
        // Request 1 is the slow one -- its call into ServiceC hangs for 3000ms.
        List<GatewayRequest> requests = new ArrayList<>();
        requests.add(new GatewayRequest(0, 0, 50));     // fast
        requests.add(new GatewayRequest(1, 5, 3030));   // the slow one (ground truth)
        requests.add(new GatewayRequest(2, 10, 60));    // fast

        // ServiceC's own log, with no correlation ID at all -- just a
        // start time and how long the call took.
        UncorrelatedLogEntry slowServiceCEntry = new UncorrelatedLogEntry(25, 3000);

        int candidateCount = 0;
        for (GatewayRequest r : requests) {
            if (r.wasActiveAt(slowServiceCEntry.startTimeMs)) {
                candidateCount++;
            }
        }

        System.out.println("ServiceC's slow log entry: started at t=" + slowServiceCEntry.startTimeMs
                + "ms, took " + slowServiceCEntry.durationMs + "ms.");
        System.out.println("Gateway requests active at t=" + slowServiceCEntry.startTimeMs + "ms: " + candidateCount);
        System.out.println("Without a correlation ID, all " + candidateCount
                + " are equally plausible -- there is no field in either log that ties them together.");
        System.out.println("Ground truth (known only to us, not to the log correlation itself): request 1.");
        System.out.println();
        System.out.println("With a trace ID carried through every span instead: the lookup is exact, every time --");
        System.out.println("no candidates to weigh, because the log entry already says which request it belongs to.");
    }
}
