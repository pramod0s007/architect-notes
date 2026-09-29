import java.util.*;

/**
 * Wraps a HeartbeatRegistry with active health checks. An instance can keep heartbeating
 * (the process is alive, the lease never expires) while failing to actually serve requests
 * correctly — a saturated thread pool, a dead downstream dependency. resolve() only returns
 * instances that are both leased (heartbeating) AND have last reported healthy.
 */
public class HealthCheckingRegistry {
    private final HeartbeatRegistry heartbeats;
    private final Map<String, Boolean> lastHealthCheck = new LinkedHashMap<>();

    HealthCheckingRegistry(HeartbeatRegistry heartbeats) {
        this.heartbeats = heartbeats;
    }

    void reportHealthCheck(String address, boolean healthy) {
        lastHealthCheck.put(address, healthy);
    }

    List<String> resolve(int currentTick) {
        List<String> live = new ArrayList<>();
        for (String address : heartbeats.resolve(currentTick)) {
            if (lastHealthCheck.getOrDefault(address, true)) {
                live.add(address);
            }
        }
        return live;
    }
}
