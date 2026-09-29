/**
 * PRINCIPLE APPLIED: compute the actual target fleet size directly from demand --
 * desiredPods = ceil(demand / (capacityPerPod * targetUtilization)) -- an HPA-inspired
 * equivalent of Kubernetes' real replica-count formula (desiredReplicas =
 * ceil(currentReplicas * currentMetricValue / desiredMetricValue)), simplified here to
 * work directly off a demand model instead of an observed resource metric. Scale
 * straight to the computed target in one step (no incremental catch-up needed, so the
 * startup lag only has to be absorbed once) instead of nudging the fleet by a fixed
 * increment or a fixed multiplier. On every under-target evaluation, only scale down
 * after the fleet has been over-provisioned for 2 consecutive evaluations in a row (a
 * stabilization window), so a single noisy low reading can't trigger a premature shrink
 * right before demand ticks back up. It also evaluates every tick instead of every 2 --
 * in this deterministic simulation, with fresh and noiseless metrics, the pressure and
 * wrong-direction versions' 2-tick interval adds an avoidable extra tick of detection
 * delay on top of the unavoidable pod-startup lag.
 */
public class Main {
    private static final int CAPACITY_PER_POD = 10;
    private static final int STARTUP_LAG_TICKS = 3;
    private static final int EVAL_INTERVAL_TICKS = 1;
    private static final double TARGET_UTILIZATION = 0.7;
    private static final int STABILIZATION_EVALS = 2;
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
        int underProvisionedStreak = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int demand = demandAt(tick);
            int capacity = cluster.capacity(tick);
            int dropped = Math.max(0, demand - capacity);
            totalDropped += dropped;
            int unusedCapacity = Math.max(0, capacity - demand);
            wastedPodTicks += (double) unusedCapacity / CAPACITY_PER_POD;

            if (tick % EVAL_INTERVAL_TICKS == 0) {
                int currentPods = cluster.totalPodCount();
                int desiredPods = Math.max(1, (int) Math.ceil(demand / (CAPACITY_PER_POD * TARGET_UTILIZATION)));

                if (desiredPods > currentPods) {
                    int addCount = desiredPods - currentPods;
                    cluster.addPods(addCount, tick, STARTUP_LAG_TICKS);
                    scaleUpEvents++;
                    underProvisionedStreak = 0;
                    System.out.println("Tick " + tick + ": demand " + demand + " -- target fleet size "
                            + desiredPods + ", scaling up by " + addCount + (addCount == 1 ? " pod" : " pods")
                            + " (ready at tick " + (tick + STARTUP_LAG_TICKS) + ")");
                } else if (desiredPods < currentPods) {
                    underProvisionedStreak++;
                    if (underProvisionedStreak >= STABILIZATION_EVALS) {
                        int removeCount = currentPods - desiredPods;
                        cluster.removePods(removeCount);
                        scaleDownEvents++;
                        underProvisionedStreak = 0;
                        System.out.println("Tick " + tick + ": demand " + demand + " -- target fleet size "
                                + desiredPods + " held for " + STABILIZATION_EVALS
                                + " consecutive evaluations, scaling down by " + removeCount + " pods");
                    } else {
                        System.out.println("Tick " + tick + ": demand " + demand + " -- target fleet size "
                                + desiredPods + " is lower, but stabilization window not yet satisfied ("
                                + underProvisionedStreak + "/" + STABILIZATION_EVALS + ") -- holding at "
                                + currentPods + " pods");
                    }
                } else {
                    underProvisionedStreak = 0;
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
