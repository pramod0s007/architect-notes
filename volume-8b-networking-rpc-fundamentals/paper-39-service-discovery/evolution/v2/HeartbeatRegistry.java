import java.util.*;

/**
 * A registry where instances register themselves and renew a lease via periodic heartbeats.
 * An instance is only returned by {@code resolve()} while its lease hasn't expired — no
 * hardcoded list, no stale addresses left behind after a redeploy or a crash.
 */
public class HeartbeatRegistry {
    private static final int TTL_TICKS = 3;
    private final Map<String, Integer> lastHeartbeatTick = new LinkedHashMap<>();

    void heartbeat(String address, int currentTick) {
        lastHeartbeatTick.put(address, currentTick);
    }

    List<String> resolve(int currentTick) {
        List<String> live = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : lastHeartbeatTick.entrySet()) {
            if (currentTick - entry.getValue() <= TTL_TICKS) {
                live.add(entry.getKey());
            }
        }
        return live;
    }
}
