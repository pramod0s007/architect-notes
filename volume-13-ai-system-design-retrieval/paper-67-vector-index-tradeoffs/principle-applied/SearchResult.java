// Result of a nearest-neighbor search: the vector found, its distance from the
// query, and how many vectors the search actually had to compare against.
class SearchResult {
    final double[] vector;
    final double distance;
    final int comparisons;

    SearchResult(double[] vector, double distance, int comparisons) {
        this.vector = vector;
        this.distance = distance;
        this.comparisons = comparisons;
    }
}
