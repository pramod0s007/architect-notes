import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DynamicBatchServer server = new DynamicBatchServer(10, 30);

        // Heavy, steady traffic: 100 requests, one arriving every 1ms.
        List<Request> heavy = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            heavy.add(new Request("req-" + i, i));
        }
        List<List<Request>> heavyBatches = server.formBatches(heavy, 99);
        long heavyTotalMs = server.totalProcessingTimeMs(heavyBatches);

        System.out.println("Heavy traffic: 100 requests, 1ms apart, batch size=10, max wait=30ms");
        System.out.println("Batches formed: " + heavyBatches.size() + " (all size-triggered -- traffic never goes quiet)");
        System.out.println("Total processing time: " + heavyTotalMs + "ms, throughput: "
            + String.format("%.2f", heavy.size() / (heavyTotalMs / 1000.0)) + " req/sec");

        // Sparse traffic: only 3 requests, 10ms apart -- the exact scenario that
        // starved completely in the wrong-direction section.
        List<Request> sparse = new ArrayList<>();
        sparse.add(new Request("req-0", 0));
        sparse.add(new Request("req-1", 10));
        sparse.add(new Request("req-2", 20));
        List<List<Request>> sparseBatches = server.formBatches(sparse, 35);

        System.out.println();
        System.out.println("Sparse traffic: 3 requests, 10ms apart, same batch size=10, max wait=30ms");
        System.out.println("Batches formed: " + sparseBatches.size());
        int processed = sparseBatches.stream().mapToInt(List::size).sum();
        System.out.println("Requests processed: " + processed + " of " + sparse.size());
        System.out.println(processed == sparse.size()
            ? "FIXED: the 30ms timeout elapsed (detected here at the 35ms observation point, since this "
              + "simulation has no continuous background timer), flushing a PARTIAL batch of all 3 "
              + "requests that had actually arrived. No request waited forever."
            : "Unexpected -- investigate.");
    }
}
