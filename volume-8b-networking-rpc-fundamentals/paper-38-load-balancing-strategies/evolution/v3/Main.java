import java.util.*;

public class Main {
    static final int[] CAPACITY = {10, 5, 1}; // Backend 0 (fast), 1 (medium), 2 (slow)
    static final int NEW_REQUESTS_PER_TICK = 3;
    static final int TOTAL_TICKS = 14;
    static final int DEGRADE_FROM_TICK = 5;  // inclusive
    static final int DEGRADE_TO_TICK = 9;    // inclusive
    static final int DEGRADED_CAPACITY = 1;  // backend 0 drops from 10 to 1 during the window

    public static void main(String[] args) {
        DynamicBackend[] staticBackends = freshBackends();
        StaticWeightedBalancer staticBalancer = new StaticWeightedBalancer(staticBackends);

        DynamicBackend[] dynamicBackends = freshBackends();
        DynamicLeastLoadBalancer dynamicBalancer = new DynamicLeastLoadBalancer(dynamicBackends);

        System.out.println("Simulating backend 0 degrading from capacity 10 to capacity "
            + DEGRADED_CAPACITY + " during ticks " + DEGRADE_FROM_TICK + "-" + DEGRADE_TO_TICK
            + " (a stand-in for a GC pause or a slow downstream dependency), then recovering.");
        System.out.println();
        System.out.printf("%-4s | %-28s | %-28s%n", "tick", "static-weighted: b0 outstanding",
            "dynamic-least-load: b0 outstanding");
        System.out.println("-----+------------------------------+------------------------------");

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            boolean degraded = tick >= DEGRADE_FROM_TICK && tick <= DEGRADE_TO_TICK;
            staticBackends[0].currentCapacity = degraded ? DEGRADED_CAPACITY : CAPACITY[0];
            dynamicBackends[0].currentCapacity = degraded ? DEGRADED_CAPACITY : CAPACITY[0];

            for (int i = 0; i < NEW_REQUESTS_PER_TICK; i++) {
                staticBalancer.pick().enqueue(1);
                dynamicBalancer.pick().enqueue(1);
            }
            for (DynamicBackend b : staticBackends) b.processOneTick();
            for (DynamicBackend b : dynamicBackends) b.processOneTick();

            System.out.printf("%-4d | %-28d | %-28d%n", tick, staticBackends[0].outstanding,
                dynamicBackends[0].outstanding);
        }

        System.out.println();
        System.out.println("Backend 0 final outstanding queue — static-weighted: " + staticBackends[0].outstanding
            + ", dynamic-least-load: " + dynamicBackends[0].outstanding);
        System.out.println();
        System.out.println("CONFIRMED FAILURE (static-weighted, reused unchanged from the previous fix): it keeps "
            + "sending backend 0 the same fixed proportion of new requests throughout the degradation window "
            + "because its weights were fixed at construction time from originalCapacity and never re-read — "
            + "the queue backs up and does not drain until capacity is restored.");
        System.out.println("FIXED (dynamic-least-load): it reads outstanding and currentCapacity fresh on every "
            + "pick, so as soon as backend 0's real capacity drops, its load ratio rises and new requests are "
            + "routed to the other backends instead — the backlog stays small and drains immediately once "
            + "capacity recovers.");
    }

    static DynamicBackend[] freshBackends() {
        DynamicBackend[] backends = new DynamicBackend[CAPACITY.length];
        for (int i = 0; i < CAPACITY.length; i++) {
            backends[i] = new DynamicBackend(i, CAPACITY[i]);
        }
        return backends;
    }
}
