public class Main {
    public static void main(String[] args) {
        MetricsOnlyObserver metrics = new MetricsOnlyObserver();

        for (int i = 0; i < 9; i++) {
            metrics.recordRequest(50); // healthy requests: Gateway -> A -> B -> C, ~50ms total
        }
        metrics.recordRequest(3050); // one request where ServiceC hung for 3000ms

        System.out.println("p99 latency: " + metrics.p99LatencyMs() + "ms (clearly elevated -- something is wrong)");
        System.out.println("Which service caused it? UNANSWERABLE from this data.");
        System.out.println("MetricsOnlyObserver never recorded per-service timing -- only the total,");
        System.out.println("so there is no method, no field, nothing to even ask the question of.");
    }
}
