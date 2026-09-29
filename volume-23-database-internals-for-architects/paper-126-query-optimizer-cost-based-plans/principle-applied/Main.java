/**
 * Demonstrates that estimating both plans' real cost per query, using
 * actual table statistics, correctly picks the cheaper plan every
 * time -- full scan for the low-selectivity query, index scan for the
 * high-selectivity one -- unlike either fixed rule from the previous
 * two scenarios, each of which was only right for one of these two
 * queries.
 */
public class Main {
    public static void main(String[] args) {
        Table table = new Table();
        QueryEngine engine = new QueryEngine(table);

        System.out.println("--- Query: status = 'active' (950 of 1000 rows) ---");
        QueryEngine.PlanResult activeResult = engine.execute("active");
        System.out.println("Index-scan estimated cost: " + activeResult.indexCost);
        System.out.println("Full-scan estimated cost: " + activeResult.scanCost);
        System.out.println("Chosen plan: " + activeResult.chosenPlan);
        System.out.println("Chosen plan's cost: " + activeResult.chosenCost);

        System.out.println();
        System.out.println("--- Query: status = 'inactive' (50 of 1000 rows) ---");
        QueryEngine.PlanResult inactiveResult = engine.execute("inactive");
        System.out.println("Index-scan estimated cost: " + inactiveResult.indexCost);
        System.out.println("Full-scan estimated cost: " + inactiveResult.scanCost);
        System.out.println("Chosen plan: " + inactiveResult.chosenPlan);
        System.out.println("Chosen plan's cost: " + inactiveResult.chosenCost);

        System.out.println();
        boolean bothOptimal = activeResult.chosenCost <= activeResult.indexCost
                && activeResult.chosenCost <= activeResult.scanCost
                && inactiveResult.chosenCost <= inactiveResult.indexCost
                && inactiveResult.chosenCost <= inactiveResult.scanCost;
        System.out.println("Both queries got the cheaper of the two available plans: " + bothOptimal);
    }
}
