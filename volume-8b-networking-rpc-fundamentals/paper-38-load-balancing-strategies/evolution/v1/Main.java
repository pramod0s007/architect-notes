public class Main {
    static final int REQUESTS = 30;
    // Heterogeneous backend capacities: units of work each backend finishes per tick.
    static final int[] CAPACITY = {10, 5, 1}; // Backend 0 (fast), 1 (medium), 2 (slow)

    public static void main(String[] args) {
        Backend[] backends = new Backend[CAPACITY.length];
        for (int i = 0; i < CAPACITY.length; i++) {
            backends[i] = new Backend(i, CAPACITY[i]);
        }

        RoundRobinBalancer balancer = new RoundRobinBalancer(backends);
        balancer.assign(REQUESTS);

        int makespan = 0;
        System.out.println("Round robin across " + backends.length + " backends with capacities "
            + java.util.Arrays.toString(CAPACITY) + ":");
        for (Backend b : backends) {
            int ticks = b.ticksToFinish();
            makespan = Math.max(makespan, ticks);
            System.out.println("Backend " + b.id + ": assigned=" + b.assigned
                + ", capacity=" + b.capacity + ", ticks to finish=" + ticks);
        }
        System.out.println("Makespan (ticks until all requests complete): " + makespan);
        System.out.println();
        System.out.println("CONFIRMED FAILURE: each backend received the same " + backends[0].assigned
            + " requests regardless of how fast it can actually process them, so the slowest backend "
            + "alone determines how long the whole batch takes, while the fastest backend sits idle "
            + "most of that time.");
    }
}
