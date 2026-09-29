/**
 * A query engine that "fixes" the previous problem by never using the
 * index at all, and always executing a full sequential scan instead --
 * a different unconditional rule, still ignoring the actual
 * selectivity of whatever filter is being run.
 */
public class QueryEngine {
    private static final int SEQUENTIAL_ACCESS_COST_PER_ROW = 1;
    private static final int RANDOM_ACCESS_COST_PER_ROW = 10;
    private static final int INDEX_TRAVERSAL_OVERHEAD = 5;

    private final Table table;

    public QueryEngine(Table table) {
        this.table = table;
    }

    /** Always executes via a full scan, and always reports that as the chosen plan. */
    public int executeAlwaysUsingFullScan() {
        return table.totalRowCount() * SEQUENTIAL_ACCESS_COST_PER_ROW;
    }

    /** The cost an index-based lookup would have had -- computed only for comparison, never chosen here. */
    public int indexCostForComparison(String status) {
        int matchCount = table.matchingPositions(status).size();
        return INDEX_TRAVERSAL_OVERHEAD + matchCount * RANDOM_ACCESS_COST_PER_ROW;
    }
}
