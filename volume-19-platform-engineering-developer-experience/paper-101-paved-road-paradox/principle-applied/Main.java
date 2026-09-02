import java.util.ArrayList;
import java.util.List;

public class Main {
    // The paved road handles the standard shape automatically. A reviewed
    // escape hatch exists for anything else -- but it still enforces the
    // same baseline requirements every paved-road service gets for free.
    static boolean deploy(Service service) {
        if (service.shape.equals("HTTP_STANDARD")) {
            return true; // paved road auto-configures health checks and logging
        }
        // Escape hatch: allowed, but only if it meets the same baseline.
        return service.isBaselineCompliant();
    }

    public static void main(String[] args) {
        List<Service> services = new ArrayList<>();
        for (int i = 1; i <= 8; i++) services.add(new Service("service-" + i, "HTTP_STANDARD", true, true));
        services.add(new Service("nightly-report-job", "BATCH_JOB", true, true));
        services.add(new Service("data-reconciliation-job", "BATCH_JOB", true, true));

        int deployed = 0;
        int baselineCompliant = 0;
        for (Service s : services) {
            if (deploy(s)) {
                deployed++;
                if (s.isBaselineCompliant()) baselineCompliant++;
            }
        }

        System.out.println("Total services: " + services.size());
        System.out.println("Deployed: " + deployed + " (8 via paved road, 2 via reviewed escape hatch)");
        System.out.println("Baseline compliant (health check + logging): " + baselineCompliant + " of " + services.size());

        System.out.println();
        System.out.println(deployed == services.size() && baselineCompliant == services.size()
            ? "FIXED: all 10 services deployed, and all 10 meet the same baseline requirements. The two "
              + "batch jobs took the escape hatch instead of the paved road, but the escape hatch still "
              + "required them to declare a health check and logging before it would let them through -- "
              + "a different path to production, not a different standard for what production requires."
            : "Unexpected -- investigate.");
    }
}
