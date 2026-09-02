import java.util.List;

// Exact nearest-neighbor search: compare the query against every single vector.
// Always correct. Cost grows linearly with how many vectors are stored.
class BruteForceIndex {
    private final List<double[]> vectors;

    BruteForceIndex(List<double[]> vectors) {
        this.vectors = vectors;
    }

    SearchResult nearest(double[] query) {
        double[] best = null;
        double bestDist = Double.MAX_VALUE;
        int comparisons = 0;
        for (double[] v : vectors) {
            comparisons++;
            double d = distance(query, v);
            if (d < bestDist) {
                bestDist = d;
                best = v;
            }
        }
        return new SearchResult(best, bestDist, comparisons);
    }

    static double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(sum);
    }
}
