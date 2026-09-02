import java.util.ArrayList;
import java.util.List;

public class Main {
    static final double ALERT_THRESHOLD = 0.10; // fire an alert above 10% error rate

    public static void main(String[] args) {
        // 950 requests from many healthy tenants, all succeeding. 50
        // requests from TenantX, ALL of which are failing -- a complete
        // outage for that one tenant. No tenant dimension exists in the
        // recorded metric at all; every request just increments one shared
        // total/failed counter.
        List<Boolean> requestFailed = new ArrayList<>();
        for (int i = 0; i < 950; i++) requestFailed.add(false); // healthy tenants
        for (int i = 0; i < 50; i++) requestFailed.add(true);   // TenantX, 100% failing

        int total = requestFailed.size();
        long failed = requestFailed.stream().filter(f -> f).count();
        double aggregateErrorRate = (double) failed / total;

        System.out.println("Total requests: " + total + ", failed: " + failed);
        System.out.println("Aggregate error rate: " + String.format("%.1f", aggregateErrorRate * 100) + "%");
        System.out.println("Alert threshold: " + String.format("%.0f", ALERT_THRESHOLD * 100) + "%");
        System.out.println("Alert fired: " + (aggregateErrorRate > ALERT_THRESHOLD));

        double tenantXRealErrorRate = 50.0 / 50.0;
        System.out.println();
        System.out.println("The hidden truth no aggregate number shows: TenantX's own error rate is "
            + String.format("%.0f", tenantXRealErrorRate * 100) + "% -- a complete outage for that tenant.");

        System.out.println();
        System.out.println(aggregateErrorRate <= ALERT_THRESHOLD
            ? "CONFIRMED FAILURE: the aggregate error rate sits comfortably under the alert threshold, "
              + "so no alert fires and every dashboard reads healthy. TenantX is in complete outage -- "
              + "100% of its requests are failing -- and that fact is mathematically invisible in a "
              + "number that averages it against 950 completely unrelated, healthy requests from other "
              + "tenants."
            : "Unexpected -- investigate.");
    }
}
