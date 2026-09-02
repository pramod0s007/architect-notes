import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // A quiet period: only 3 requests arrive before the observation window
        // ends. Real traffic is bursty -- this is not a contrived edge case, it's
        // every off-peak hour of every real service.
        List<String> arrivedRequests = new ArrayList<>();
        arrivedRequests.add("req-0");
        arrivedRequests.add("req-1");
        arrivedRequests.add("req-2");

        WaitForFullBatchServer server = new WaitForFullBatchServer(10);
        List<String> processable = server.processableRequests(arrivedRequests);

        System.out.println("Requests arrived during observation window: " + arrivedRequests.size());
        System.out.println("Required batch size (no timeout fallback): 10");
        System.out.println("Requests actually processable: " + processable.size());
        System.out.println(processable.isEmpty()
            ? "STILL FAILS: 0 of " + arrivedRequests.size() + " requests were processed. They aren't "
              + "wrong, or lost -- they're just waiting, indefinitely, for 7 more requests that may not "
              + "arrive for minutes during a quiet period. A batching strategy with no timeout doesn't "
              + "trade a little latency for throughput -- under real, uneven traffic, it can starve "
              + "requests completely. The fix isn't 'smaller batches' or 'bigger batches' -- it's "
              + "triggering a batch on whichever comes first: full, OR a maximum wait time elapsed."
            : "Unexpected -- investigate.");
    }
}
