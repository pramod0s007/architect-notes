import java.util.*;

public class Main {
    static final int TOTAL_TICKS = 10;

    public static void main(String[] args) {
        HeartbeatRegistry registry = new HeartbeatRegistry();
        List<String> steady = Arrays.asList("10.0.0.2:8080", "10.0.0.3:8080");

        System.out.println("tick | heartbeats sent this tick             | resolve() returns");
        System.out.println("-----+----------------------------------------+-------------------");
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            List<String> heartbeatsThisTick = new ArrayList<>(steady);

            if (tick <= 3) {
                heartbeatsThisTick.add("10.0.0.1:8080"); // original instance, still alive
            }
            // 10.0.0.1 crashes after tick 3 and never heartbeats again.

            if (tick >= 5) {
                heartbeatsThisTick.add("10.0.0.4:8080"); // new instance from the redeploy
            }

            for (String address : heartbeatsThisTick) {
                registry.heartbeat(address, tick);
            }

            List<String> live = registry.resolve(tick);
            System.out.printf("%-4d | %-38s | %s%n", tick, heartbeatsThisTick, live);
        }

        System.out.println();
        System.out.println("FIXED: no hardcoded address list anywhere. 10.0.0.1 drops out of resolve() on its "
            + "own once its lease expires (3 ticks after its last heartbeat, at tick 7), and 10.0.0.4 starts "
            + "appearing the moment it begins heartbeating at tick 5 — the registry reflects reality instead "
            + "of a snapshot taken at deploy time.");
    }
}
