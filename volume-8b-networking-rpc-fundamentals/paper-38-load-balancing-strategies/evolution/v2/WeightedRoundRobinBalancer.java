/**
 * Splits requests across backends in proportion to each backend's own capacity,
 * using largest-remainder apportionment so the shares sum exactly to the request count.
 */
public class WeightedRoundRobinBalancer {
    private final Backend[] backends;

    WeightedRoundRobinBalancer(Backend[] backends) {
        this.backends = backends;
    }

    void assign(int requests) {
        int totalCapacity = 0;
        for (Backend b : backends) totalCapacity += b.capacity;

        double[] exactShare = new double[backends.length];
        int assignedSoFar = 0;
        for (int i = 0; i < backends.length; i++) {
            exactShare[i] = requests * (double) backends[i].capacity / totalCapacity;
            backends[i].assigned = (int) Math.floor(exactShare[i]);
            assignedSoFar += backends[i].assigned;
        }

        int remainder = requests - assignedSoFar;
        Integer[] order = new Integer[backends.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) -> Double.compare(
            exactShare[b] - Math.floor(exactShare[b]),
            exactShare[a] - Math.floor(exactShare[a])));
        for (int i = 0; i < remainder; i++) {
            backends[order[i]].assigned++;
        }
    }
}
