import java.util.ArrayList;
import java.util.List;

public class Main {
    // Overcorrection: the escape hatch exists to unblock the batch jobs --
    // it isn't the pressure case's mistake, nothing is rejected outright.
    // But it enforces nothing at all, so anything sent through it is
    // deployed unconditionally, whether or not it meets any baseline.
    static boolean deploy(Service service) {
        if (!service.usesEscapeHatch) {
            return true; // paved road, auto-configures health checks and logging
        }
        return true; // escape hatch: always allowed, nothing checked
    }

    public static void main(String[] args) {
        List<Service> services = new ArrayList<>();
        // 4 standard services stay on the paved road.
        for (int i = 1; i <= 4; i++) services.add(new Service("service-" + i, "HTTP_STANDARD", false, true, true));
        // 4 MORE standard services -- perfectly capable of using the paved
        // road -- route through the escape hatch anyway, because it's less
        // friction, and nothing stops them.
        for (int i = 5; i <= 8; i++) services.add(new Service("service-" + i, "HTTP_STANDARD", true, false, false));
        // The 2 batch jobs that genuinely needed the escape hatch.
        services.add(new Service("nightly-report-job", "BATCH_JOB", true, false, false));
        services.add(new Service("data-reconciliation-job", "BATCH_JOB", true, false, false));

        int deployed = 0, baselineCompliant = 0, viaEscapeHatch = 0;
        for (Service s : services) {
            if (deploy(s)) deployed++;
            if (s.isBaselineCompliant()) baselineCompliant++;
            if (s.usesEscapeHatch) viaEscapeHatch++;
        }

        System.out.println("Total services: " + services.size());
        System.out.println("Deployed: " + deployed + " (nothing rejected, escape hatch enforces nothing)");
        System.out.println("Routed through the unrestricted escape hatch: " + viaEscapeHatch + " of " + services.size());
        System.out.println("Baseline compliant (health check + logging): " + baselineCompliant + " of " + services.size());

        System.out.println();
        System.out.println(deployed == services.size() && baselineCompliant < 5
            ? "CONFIRMED FAILURE: every service deployed -- the escape hatch really did unblock the 2 "
              + "batch jobs, exactly as intended. But 4 more services that were perfectly capable of "
              + "using the paved road routed through the same unrestricted hatch anyway, because it was "
              + "less friction than doing it properly, and nothing enforced otherwise. 6 of 10 services "
              + "now have no health check and no logging -- a majority of production traffic has "
              + "regressed to exactly the inconsistency the paved road existed to prevent."
            : "Unexpected -- investigate.");
    }
}
