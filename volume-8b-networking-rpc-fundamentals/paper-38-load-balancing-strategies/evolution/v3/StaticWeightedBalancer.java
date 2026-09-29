/**
 * Smooth weighted round robin (as used by NGINX): each pick, every backend's running
 * "current weight" grows by its static weight; the backend with the highest current weight
 * is chosen, then its current weight is reduced by the total weight. Deterministic, evenly
 * spread — but the weights are fixed at construction time from {@code originalCapacity}, so
 * it has no way to know a backend has slowed down since then.
 */
public class StaticWeightedBalancer {
    private final DynamicBackend[] backends;
    private final int[] weight;
    private final int[] currentWeight;
    private final int totalWeight;

    StaticWeightedBalancer(DynamicBackend[] backends) {
        this.backends = backends;
        this.weight = new int[backends.length];
        this.currentWeight = new int[backends.length];
        int total = 0;
        for (int i = 0; i < backends.length; i++) {
            weight[i] = backends[i].originalCapacity;
            total += weight[i];
        }
        this.totalWeight = total;
    }

    DynamicBackend pick() {
        int best = 0;
        for (int i = 0; i < backends.length; i++) {
            currentWeight[i] += weight[i];
            if (currentWeight[i] > currentWeight[best]) best = i;
        }
        currentWeight[best] -= totalWeight;
        return backends[best];
    }
}
