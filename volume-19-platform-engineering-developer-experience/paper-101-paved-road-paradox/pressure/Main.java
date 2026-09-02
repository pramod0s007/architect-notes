import java.util.ArrayList;
import java.util.List;

public class Main {
    // The platform supports exactly one deployment shape. A service that
    // doesn't fit it has no path to production at all -- there is no
    // second option, reviewed or otherwise.
    static boolean deploy(Service service) {
        return service.shape.equals("HTTP_STANDARD");
    }

    public static void main(String[] args) {
        List<Service> services = new ArrayList<>();
        for (int i = 1; i <= 8; i++) services.add(new Service("service-" + i, "HTTP_STANDARD"));
        services.add(new Service("nightly-report-job", "BATCH_JOB"));
        services.add(new Service("data-reconciliation-job", "BATCH_JOB"));

        int deployed = 0, rejected = 0;
        List<String> rejectedNames = new ArrayList<>();
        for (Service s : services) {
            if (deploy(s)) {
                deployed++;
            } else {
                rejected++;
                rejectedNames.add(s.name);
            }
        }

        System.out.println("Total services: " + services.size());
        System.out.println("Deployed via the paved road: " + deployed);
        System.out.println("Rejected -- no path to production at all: " + rejected + " " + rejectedNames);

        System.out.println();
        System.out.println(rejected == 2
            ? "CONFIRMED FAILURE: two legitimate production services -- a nightly report and a data "
              + "reconciliation job, both genuinely needed by the business -- cannot deploy through the "
              + "platform at all, because the platform offers exactly one shape and neither of these "
              + "services is shaped like a long-running HTTP server. The paved road didn't just fail to "
              + "help them; it left them with no road at all."
            : "Unexpected -- investigate.");
    }
}
