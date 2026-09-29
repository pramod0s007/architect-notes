import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Addresses baked into config at the last deploy.
        StaticServiceLocator locator = new StaticServiceLocator(
            Arrays.asList("10.0.0.1:8080", "10.0.0.2:8080", "10.0.0.3:8080"));

        // A redeploy happens: 10.0.0.1 is torn down and replaced by a new instance at
        // 10.0.0.4 — normal behavior for a rolling deploy on ephemeral infrastructure.
        Set<String> actuallyRunning = new HashSet<>(
            Arrays.asList("10.0.0.2:8080", "10.0.0.3:8080", "10.0.0.4:8080"));

        List<String> resolved = locator.resolve();
        int failedCalls = 0;
        System.out.println("Static locator still returns: " + resolved);
        System.out.println("Actually running right now:  " + actuallyRunning);
        for (String address : resolved) {
            boolean live = actuallyRunning.contains(address);
            System.out.println("Call to " + address + " -> " + (live ? "OK" : "CONNECTION REFUSED"));
            if (!live) failedCalls++;
        }

        System.out.println();
        System.out.println("Failed calls: " + failedCalls + " out of " + resolved.size());
        System.out.println();
        System.out.println("CONFIRMED FAILURE: the locator's address list was fixed at construction time. "
            + "A normal redeploy retired 10.0.0.1 and brought up 10.0.0.4, but nothing tells the locator "
            + "that happened — every call to the retired address fails, and the new instance never "
            + "receives a single request.");
    }
}
