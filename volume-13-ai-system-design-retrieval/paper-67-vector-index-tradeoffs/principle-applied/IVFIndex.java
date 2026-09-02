import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Inverted-file index: vectors are assigned at build time to their nearest of a
// fixed set of centroids ("buckets"). At search time, only the nprobe nearest
// buckets to the query are scanned -- not the whole dataset. Fewer comparisons
// than brute force; correctness now depends on nprobe being high enough to reach
// every bucket that could plausibly contain the true nearest neighbor.
class IVFIndex {
    private final List<double[]> centroids;
    private final Map<Integer, List<double[]>> buckets = new HashMap<>();

    IVFIndex(List<double[]> centroids, List<double[]> vectors) {
        this.centroids = centroids;
        for (int i = 0; i < centroids.size(); i++) {
            buckets.put(i, new ArrayList<>());
        }
        for (double[] v : vectors) {
            int nearest = nearestCentroidIndex(v);
            buckets.get(nearest).add(v);
        }
    }

    private int nearestCentroidIndex(double[] v) {
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < centroids.size(); i++) {
            double d = BruteForceIndex.distance(v, centroids.get(i));
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    SearchResult search(double[] query, int nprobe) {
        if (nprobe <= 0) {
            throw new IllegalArgumentException("nprobe must be at least 1");
        }
        List<Integer> centroidOrder = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            centroidOrder.add(i);
        }
        centroidOrder.sort((a, b) -> Double.compare(
            BruteForceIndex.distance(query, centroids.get(a)),
            BruteForceIndex.distance(query, centroids.get(b))));

        double[] best = null;
        double bestDist = Double.MAX_VALUE;
        int comparisons = 0;
        for (int i = 0; i < nprobe && i < centroidOrder.size(); i++) {
            for (double[] v : buckets.get(centroidOrder.get(i))) {
                comparisons++;
                double d = BruteForceIndex.distance(query, v);
                if (d < bestDist) {
                    bestDist = d;
                    best = v;
                }
            }
        }
        return new SearchResult(best, bestDist, comparisons);
    }
}
