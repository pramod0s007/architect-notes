/**
 * WRONG DIRECTION: "fix" the dropped-request problem by scaling far more aggressively --
 * on every over-threshold evaluation, double the entire fleet (add currentPodCount pods)
 * instead of adding just 1; on every under-threshold evaluation, halve it. This
 * substantially cuts the drops -- capacity ramps far faster than the +1-per-eval
 * baseline could manage -- though it still doesn't reach zero. But the
 * fix has no sense of how much capacity is actually needed -- it only knows "more" and
 * "less" -- so it wildly overshoots in both directions: the fleet balloons far past what
 * the spike ever required, then gets slashed back once demand recedes, producing a large
 * number of pod-ticks that were provisioned and paid for but never used.
 */
public class Main {
    private static final int CAPACITY_PER_POD = 10;
    private static final int STARTUP_LAG_TICKS = 3;
    private static final int EVAL_INTERVAL_TICKS = 2;
    private static final double SCALE_UP_THRESHOLD = 0.8;
    private static final double SCALE_DOWN_THRESHOLD = 0.3;
    private static final int TOTAL_TICKS = 40;

    private static int demandAt(int tick) {
        if (tick >= 5 && tick <= 24) {
            return 100;
        }
        return 18;
    }

    public static void main(String[] args) {
        Cluster cluster = new Cluster(2, CAPACITY_PER_POD);
        int totalDropped = 0;
        double wastedPodTicks = 0;
        int scaleUpEvents = 0;
        int scaleDownEvents = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int demand = demandAt(tick);
            int capacity = cluster.capacity(tick);
            int dropped = Math.max(0, demand - capacity);
            totalDropped += dropped;
            int unusedCapacity = Math.max(0, capacity - demand);
            wastedPodTicks += (double) unusedCapacity / CAPACITY_PER_POD;

            if (tick % EVAL_INTERVAL_TICKS == 0) {
                double utilization = capacity == 0 ? Double.MAX_VALUE : (double) demand / capacity;
                if (utilization > SCALE_UP_THRESHOLD) {
                    int addCount = cluster.totalPodCount();
                    cluster.addPods(addCount, tick, STARTUP_LAG_TICKS);
                    scaleUpEvents++;
                    System.out.println("Tick " + tick + ": utilization " + String.format("%.2f", utilization)
                            + " over threshold -- doubling fleet, adding " + addCount + " pods (ready at tick "
                            + (tick + STARTUP_LAG_TICKS) + ")");
                } else if (utilization < SCALE_DOWN_THRESHOLD && cluster.totalPodCount() > 1) {
                    int removeCount = Math.max(1, cluster.totalPodCount() / 2);
                    cluster.removePods(removeCount);
                    scaleDownEvents++;
                    System.out.println("Tick " + tick + ": utilization " + String.format("%.2f", utilization)
                            + " under threshold -- halving fleet, removing " + removeCount + " pods");
                }
            }

            if (dropped > 0) {
                System.out.println("Tick " + tick + ": demand " + demand + " > capacity " + capacity
                        + " -- " + dropped + " requests dropped");
            }
        }

        System.out.println();
        System.out.println("Total requests dropped: " + totalDropped + ", wasted pod-ticks: " + String.format("%.1f", wastedPodTicks)
                + ", scale-up events: " + scaleUpEvents + ", scale-down events: " + scaleDownEvents
                + ", final pod count: " + cluster.totalPodCount());
    }
}
