/**
 * Picks the backend with the lowest live load ratio: outstanding requests divided by its
 * *current* capacity, not the capacity it had when the balancer was built. Because it reads
 * {@code currentCapacity} and {@code outstanding} fresh on every pick, it reacts to a backend
 * slowing down in real time instead of trusting a number that's gone stale.
 */
public class DynamicLeastLoadBalancer {
    private final DynamicBackend[] backends;

    DynamicLeastLoadBalancer(DynamicBackend[] backends) {
        this.backends = backends;
    }

    DynamicBackend pick() {
        DynamicBackend best = backends[0];
        double bestRatio = loadRatio(best);
        for (int i = 1; i < backends.length; i++) {
            double ratio = loadRatio(backends[i]);
            if (ratio < bestRatio) {
                bestRatio = ratio;
                best = backends[i];
            }
        }
        return best;
    }

    private double loadRatio(DynamicBackend b) {
        // A backend with currentCapacity 0 (fully stalled) is maximally undesirable.
        if (b.currentCapacity <= 0) return Double.MAX_VALUE;
        return (double) b.outstanding / b.currentCapacity;
    }
}
