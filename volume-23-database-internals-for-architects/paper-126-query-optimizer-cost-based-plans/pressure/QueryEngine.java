/**
 * A query engine that always uses the index for an equality filter,
 * regardless of how many rows actually match. This ignores selectivity
 * entirely: an index lookup followed by fetching each matching row is
 * a random-access operation per row, which is fine when few rows
 * match, but ruinous when most of the table matches.
 */
public class QueryEngine {
    private static final int SEQUENTIAL_ACCESS_COST_PER_ROW = 1;
    private static final int RANDOM_ACCESS_COST_PER_ROW = 10;
    private static final int INDEX_TRAVERSAL_OVERHEAD = 5;

    private final Table table;

    public QueryEngine(Table table) {
        this.table = table;
    }

    /** Always executes via the index, and always reports that as the chosen plan. */
    public int executeAlwaysUsingIndex(String status) {
        int matchCount = table.matchingPositions(status).size();
        return INDEX_TRAVERSAL_OVERHEAD + matchCount * RANDOM_ACCESS_COST_PER_ROW;
    }

    /** The cost a full sequential scan would have had -- computed only for comparison, never chosen here. */
    public int fullScanCostForComparison() {
        return table.totalRowCount() * SEQUENTIAL_ACCESS_COST_PER_ROW;
    }
}
