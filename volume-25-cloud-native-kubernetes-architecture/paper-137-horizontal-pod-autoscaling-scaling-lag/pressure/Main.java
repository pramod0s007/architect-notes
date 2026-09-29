/**
 * PRESSURE: a naive HPA-style autoscaler evaluates utilization every 2 ticks and, when
 * over threshold, adds exactly 1 pod -- but each new pod takes 3 ticks (STARTUP_LAG_TICKS)
 * before it actually counts toward capacity. When demand suddenly jumps 5x (a traffic
 * spike from tick 5 to tick 24), the incremental +1-pod-per-evaluation rule can't ramp
 * capacity fast enough to keep pace, and requests are dropped for most of the spike's
 * duration even though the autoscaler is actively (correctly) trying to react the whole
 * time.
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
                    cluster.addPods(1, tick, STARTUP_LAG_TICKS);
                    scaleUpEvents++;
                    System.out.println("Tick " + tick + ": utilization " + String.format("%.2f", utilization)
                            + " over threshold -- scaling up by 1 pod (ready at tick " + (tick + STARTUP_LAG_TICKS) + ")");
                } else if (utilization < SCALE_DOWN_THRESHOLD && cluster.totalPodCount() > 1) {
                    cluster.removePods(1);
                    scaleDownEvents++;
                    System.out.println("Tick " + tick + ": utilization " + String.format("%.2f", utilization)
                            + " under threshold -- scaling down by 1 pod");
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
