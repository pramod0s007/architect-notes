import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int SHARED_CAPACITY = 100; // total requests this system can process per window

    public static void main(String[] args) {
        // Two tenants share one system with no per-tenant awareness at all.
        // Tenant A is a large, bursty customer wanting 150 requests this
        // window. Tenant B is a small, well-behaved customer wanting only 10.
        List<String> combinedArrivalOrder = new ArrayList<>();
        for (int i = 0; i < 150; i++) combinedArrivalOrder.add("TenantA");
        for (int i = 0; i < 10; i++) combinedArrivalOrder.add("TenantB");

        int processedA = 0, processedB = 0;
        for (int i = 0; i < SHARED_CAPACITY && i < combinedArrivalOrder.size(); i++) {
            if (combinedArrivalOrder.get(i).equals("TenantA")) processedA++;
            else processedB++;
        }

        System.out.println("Shared capacity: " + SHARED_CAPACITY + " requests/window, no per-tenant limits.");
        System.out.println("Tenant A wanted 150, Tenant B wanted 10 -- A's burst arrives first in the window.");
        System.out.println("Tenant A processed: " + processedA + " of 150 requested");
        System.out.println("Tenant B processed: " + processedB + " of 10 requested");

        System.out.println();
        System.out.println(processedB == 0
            ? "CONFIRMED FAILURE: Tenant B, a small customer who asked for a tiny fraction of total "
              + "capacity, got ZERO requests processed -- not because the system lacked capacity for "
              + "B's request, but because Tenant A's unrelated burst consumed the entire shared pool "
              + "first. B's own usage was never the problem; the system just had no concept of a "
              + "tenant boundary at all."
            : "Unexpected -- investigate.");
    }
}
