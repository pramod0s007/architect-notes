/**
 * PRESSURE: a brief, shared downstream blip (already recovered) caused 50 client sidecars'
 * in-flight requests to fail simultaneously at tick 0. Every sidecar's retry policy is a
 * fixed 1-tick interval with no backoff and no jitter -- simple, easy to reason about in
 * isolation, and exactly what causes the storm. Downstream is healthy again from tick 1
 * onward, but only has 10 units/tick of spare capacity above its steady baseline traffic.
 * All 50 clients retry at the same instant every single tick until they succeed, so the
 * queue drains at a flat 10/tick regardless of how many are still waiting -- the fix isn't
 * "retry faster," the bottleneck is capacity, and hammering it in lockstep doesn't help.
 */
public class Main {
    private static final int TOTAL_CLIENTS = 50;
    private static final int SPARE_CAPACITY_PER_TICK = 10;
    private static final int MAX_TICKS = 100;

    public static void main(String[] args) {
        Downstream downstream = new Downstream(SPARE_CAPACITY_PER_TICK);
        int pending = TOTAL_CLIENTS;
        long totalAttempts = 0;
        int stormTicks = 0;
        int drainTick = -1;

        for (int tick = 1; tick <= MAX_TICKS && pending > 0; tick++) {
            int demand = pending;
            totalAttempts += demand;
            int succeeded = downstream.serve(demand);
            int stillFailed = demand - succeeded;
            if (demand > succeeded) {
                stormTicks++;
            }

            System.out.println("Tick " + tick + ": " + demand + " clients retry simultaneously, downstream serves "
                    + succeeded + ", " + stillFailed + " fail and retry again next tick");

            pending = stillFailed;
            if (pending == 0) {
                drainTick = tick;
            }
        }

        System.out.println();
        System.out.println("Total retry attempts issued: " + totalAttempts + ", ticks downstream was over capacity: "
                + stormTicks + ", tick fully drained: " + drainTick);
    }
}
