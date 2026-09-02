import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // A small, hand-built 2D dataset: 4 well-separated clusters (documents that
        // are semantically similar cluster together in embedding space), plus one
        // point deliberately placed near a cluster boundary. This exact dataset is
        // reused unchanged in principle-applied/ and wrong-direction/.
        List<double[]> points = new ArrayList<>();
        // Cluster A, centroid (0,0)
        points.add(new double[]{0, 0});
        points.add(new double[]{5, 3});
        points.add(new double[]{-3, 4});
        points.add(new double[]{2, -5});
        points.add(new double[]{-4, -2});
        points.add(new double[]{3, 6});
        // Cluster B, centroid (50,0) -- includes the boundary point (26,0)
        points.add(new double[]{50, 0});
        points.add(new double[]{45, 3});
        points.add(new double[]{55, -4});
        points.add(new double[]{48, 6});
        points.add(new double[]{52, -2});
        points.add(new double[]{26, 0});
        // Cluster C, centroid (0,50)
        points.add(new double[]{0, 50});
        points.add(new double[]{5, 53});
        points.add(new double[]{-3, 54});
        points.add(new double[]{2, 45});
        // Cluster D, centroid (50,50)
        points.add(new double[]{50, 50});
        points.add(new double[]{45, 53});
        points.add(new double[]{55, 46});
        points.add(new double[]{48, 54});

        double[] query = {24, 0};

        BruteForceIndex index = new BruteForceIndex(points);
        SearchResult result = index.nearest(query);

        System.out.println("Dataset size: " + points.size() + " vectors.");
        System.out.println("Query: (" + query[0] + ", " + query[1] + ")");
        System.out.println("Brute-force nearest neighbor: (" + result.vector[0] + ", " + result.vector[1] + "), "
            + "distance=" + result.distance + ", comparisons=" + result.comparisons);
        System.out.println("Comparisons always equal dataset size -- exact, but O(N).");

        // Same mechanism, larger N, to show the comparisons count actually scaling
        // linearly -- this is the pressure a real knowledge base runs into.
        for (int n : new int[]{1_000, 10_000, 100_000}) {
            List<double[]> large = randomVectors(n, 8, 42);
            long start = System.nanoTime();
            SearchResult r = new BruteForceIndex(large).nearest(large.get(0));
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("N=" + n + ": comparisons=" + r.comparisons + ", elapsed=" + elapsedMs + "ms");
        }
    }

    static List<double[]> randomVectors(int n, int dim, long seed) {
        Random r = new Random(seed);
        List<double[]> vectors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double[] v = new double[dim];
            for (int d = 0; d < dim; d++) {
                v[d] = r.nextDouble() * 100;
            }
            vectors.add(v);
        }
        return vectors;
    }
}
