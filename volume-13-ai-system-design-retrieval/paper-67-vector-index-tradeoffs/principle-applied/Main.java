import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<double[]> points = new ArrayList<>();
        points.add(new double[]{0, 0});
        points.add(new double[]{5, 3});
        points.add(new double[]{-3, 4});
        points.add(new double[]{2, -5});
        points.add(new double[]{-4, -2});
        points.add(new double[]{3, 6});
        points.add(new double[]{50, 0});
        points.add(new double[]{45, 3});
        points.add(new double[]{55, -4});
        points.add(new double[]{48, 6});
        points.add(new double[]{52, -2});
        points.add(new double[]{26, 0});
        points.add(new double[]{0, 50});
        points.add(new double[]{5, 53});
        points.add(new double[]{-3, 54});
        points.add(new double[]{2, 45});
        points.add(new double[]{50, 50});
        points.add(new double[]{45, 53});
        points.add(new double[]{55, 46});
        points.add(new double[]{48, 54});

        List<double[]> centroids = new ArrayList<>();
        centroids.add(new double[]{0, 0});
        centroids.add(new double[]{50, 0});
        centroids.add(new double[]{0, 50});
        centroids.add(new double[]{50, 50});

        double[] query = {24, 0};

        SearchResult exact = new BruteForceIndex(points).nearest(query);
        System.out.println("Brute-force ground truth: (" + exact.vector[0] + ", " + exact.vector[1] + "), "
            + "distance=" + exact.distance + ", comparisons=" + exact.comparisons);

        IVFIndex ivf = new IVFIndex(centroids, points);
        SearchResult approx = ivf.search(query, 2);
        System.out.println("IVF, nprobe=2: (" + approx.vector[0] + ", " + approx.vector[1] + "), "
            + "distance=" + approx.distance + ", comparisons=" + approx.comparisons);

        boolean matches = approx.distance == exact.distance;
        System.out.println("Matches brute-force exactly: " + matches);
        System.out.println("Comparisons: " + approx.comparisons + "/" + exact.comparisons + " = "
            + Math.round(100.0 * approx.comparisons / exact.comparisons) + "% of brute force, same answer.");
    }
}
