public class Main {
    static final int REQUESTS = 30;
    static final int[] CAPACITY = {10, 5, 1}; // Backend 0 (fast), 1 (medium), 2 (slow)

    public static void main(String[] args) {
        Backend[] backends = new Backend[CAPACITY.length];
        for (int i = 0; i < CAPACITY.length; i++) {
            backends[i] = new Backend(i, CAPACITY[i]);
        }

        WeightedRoundRobinBalancer balancer = new WeightedRoundRobinBalancer(backends);
        balancer.assign(REQUESTS);

        int makespan = 0;
        System.out.println("Weighted round robin across " + backends.length + " backends with capacities "
            + java.util.Arrays.toString(CAPACITY) + ":");
        for (Backend b : backends) {
            int ticks = b.ticksToFinish();
            makespan = Math.max(makespan, ticks);
            System.out.println("Backend " + b.id + ": assigned=" + b.assigned
                + ", capacity=" + b.capacity + ", ticks to finish=" + ticks);
        }
        System.out.println("Makespan (ticks until all requests complete): " + makespan);
        System.out.println();
        System.out.println("FIXED: requests are split in proportion to each backend's real capacity, so all "
            + "three backends finish at nearly the same time instead of the whole batch waiting on the "
            + "slowest one.");
    }
}
