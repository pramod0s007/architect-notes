import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add("req-" + i);
        }

        NoBatchingServer server = new NoBatchingServer();
        long totalMs = server.totalProcessingTimeMs(requests);
        double throughput = requests.size() / (totalMs / 1000.0);

        System.out.println("Requests: " + requests.size() + ", no batching (batch size = 1 always)");
        System.out.println("Per-request cost: " + NoBatchingServer.FIXED_OVERHEAD_MS + "ms fixed overhead + "
            + NoBatchingServer.PER_ITEM_COST_MS + "ms compute, paid on EVERY single request");
        System.out.println("Total processing time: " + totalMs + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/sec");
        System.out.println("CONFIRMED: fixed overhead is paid " + requests.size() + " separate times -- "
            + (NoBatchingServer.FIXED_OVERHEAD_MS * requests.size()) + "ms of the "
            + totalMs + "ms total is pure per-call overhead, not actual compute.");
    }
}
