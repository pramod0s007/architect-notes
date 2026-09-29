/**
 * Demonstrates that "never use the index" fixes the previous query
 * (status = active, low selectivity) but performs poorly on a
 * different, highly selective query (status = inactive, only 50 of
 * 1,000 rows) -- the same table, the same index, a different rule,
 * and now a different query pays the wrong-direction cost instead.
 */
public class Main {
    public static void main(String[] args) {
        Table table = new Table();
        QueryEngine engine = new QueryEngine(table);

        System.out.println("--- Query: status = 'active' (950 of 1000 rows) ---");
        int activeChosenCost = engine.executeAlwaysUsingFullScan();
        int activeIndexCost = engine.indexCostForComparison("active");
        System.out.println("Chosen plan: full scan (unconditional rule)");
        System.out.println("Chosen plan's estimated cost: " + activeChosenCost);
        System.out.println("Index-scan cost would have been: " + activeIndexCost);
        System.out.println("This query is now cheaper than before: " + (activeChosenCost < activeIndexCost));

        System.out.println();
        System.out.println("--- Query: status = 'inactive' (50 of 1000 rows) ---");
        int inactiveChosenCost = engine.executeAlwaysUsingFullScan();
        int inactiveIndexCost = engine.indexCostForComparison("inactive");
        System.out.println("Chosen plan: full scan (unconditional rule)");
        System.out.println("Chosen plan's estimated cost: " + inactiveChosenCost);
        System.out.println("Index-scan cost would have been: " + inactiveIndexCost);
        System.out.println("Chosen plan was more expensive: " + (inactiveChosenCost > inactiveIndexCost));
    }
}
