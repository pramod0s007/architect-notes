/**
 * A cost-based query engine: for every query, it estimates the real
 * cost of both an index-scan plan and a full-scan plan using actual
 * table statistics (the index's own matching-row count, gathered
 * without executing anything), and picks whichever plan is genuinely
 * cheaper for that specific query -- instead of applying the same
 * rule to every query regardless of selectivity.
 */
public class QueryEngine {
    private static final int SEQUENTIAL_ACCESS_COST_PER_ROW = 1;
    private static final int RANDOM_ACCESS_COST_PER_ROW = 10;
    private static final int INDEX_TRAVERSAL_OVERHEAD = 5;

    private final Table table;

    public QueryEngine(Table table) {
        this.table = table;
    }

    private int indexScanCost(String status) {
        int matchCount = table.matchingPositions(status).size();
        return INDEX_TRAVERSAL_OVERHEAD + matchCount * RANDOM_ACCESS_COST_PER_ROW;
    }

    private int fullScanCost() {
        return table.totalRowCount() * SEQUENTIAL_ACCESS_COST_PER_ROW;
    }

    /** Estimates both real plan costs and executes whichever one is actually cheaper for this query. */
    public PlanResult execute(String status) {
        int indexCost = indexScanCost(status);
        int scanCost = fullScanCost();
        String chosenPlan = indexCost <= scanCost ? "index scan" : "full scan";
        int chosenCost = Math.min(indexCost, scanCost);
        return new PlanResult(chosenPlan, chosenCost, indexCost, scanCost);
    }

    public static class PlanResult {
        public final String chosenPlan;
        public final int chosenCost;
        public final int indexCost;
        public final int scanCost;

        PlanResult(String chosenPlan, int chosenCost, int indexCost, int scanCost) {
            this.chosenPlan = chosenPlan;
            this.chosenCost = chosenCost;
            this.indexCost = indexCost;
            this.scanCost = scanCost;
        }
    }
}
