import java.util.*;

public class Main {
    static final int TOTAL_TICKS = 10;
    static final int SICK_FROM_TICK = 4;   // inclusive
    static final int SICK_TO_TICK = 7;     // inclusive

    public static void main(String[] args) {
        HeartbeatRegistry heartbeats = new HeartbeatRegistry();
        HealthCheckingRegistry withHealthChecks = new HealthCheckingRegistry(heartbeats);
        List<String> instances = Arrays.asList("10.0.0.2:8080", "10.0.0.3:8080");
        String sickInstance = "10.0.0.3:8080"; // keeps heartbeating throughout, health degrades mid-run

        System.out.println("Instance " + sickInstance + " keeps heartbeating every tick (process is alive), "
            + "but its own health checks fail during ticks " + SICK_FROM_TICK + "-" + SICK_TO_TICK
            + " (a stand-in for a saturated thread pool or a dead downstream dependency), then recover.");
        System.out.println();
        System.out.println("tick | heartbeat-only resolve()               | health-checked resolve()");
        System.out.println("-----+----------------------------------------+-------------------------");

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            for (String address : instances) {
                heartbeats.heartbeat(address, tick); // never stops, on schedule every tick
            }
            boolean sick = tick >= SICK_FROM_TICK && tick <= SICK_TO_TICK;
            withHealthChecks.reportHealthCheck(sickInstance, !sick);

            List<String> heartbeatOnly = heartbeats.resolve(tick);
            List<String> healthChecked = withHealthChecks.resolve(tick);
            System.out.printf("%-4d | %-38s | %s%n", tick, heartbeatOnly, healthChecked);
        }

        System.out.println();
        System.out.println("CONFIRMED FAILURE (heartbeat-only registry, reused unchanged from the previous fix): "
            + sickInstance + " never stops heartbeating, so its lease never expires and it stays in every "
            + "resolve() call throughout the sick window — callers keep getting routed to an instance that "
            + "cannot actually serve their requests.");
        System.out.println("FIXED (health-checked registry): an independent health probe, not the heartbeat "
            + "channel, decides routability — " + sickInstance + " is excluded the moment its health check "
            + "fails and reappears the moment it recovers, regardless of what its heartbeat is doing.");
    }
}
