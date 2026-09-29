/**
 * Demonstrates that unconditionally using an index for every equality
 * filter is a poor rule when the filtered value isn't selective: the
 * query "status = active" matches 950 of 1,000 rows, so the index
 * path pays a random-access cost per matching row for the vast
 * majority of the table, when a plain sequential scan would have been
 * far cheaper.
 */
public class Main {
    public static void main(String[] args) {
        Table table = new Table();
        QueryEngine engine = new QueryEngine(table);

        String query = "active";
        int chosenCost = engine.executeAlwaysUsingIndex(query);
        int fullScanCost = engine.fullScanCostForComparison();

        System.out.println("Query: status = '" + query + "'");
        System.out.println("Matching rows: " + table.matchingPositions(query).size() + " of " + table.totalRowCount());
        System.out.println("Chosen plan: index scan (unconditional rule)");
        System.out.println("Chosen plan's estimated cost: " + chosenCost);
        System.out.println("Full-scan cost would have been: " + fullScanCost);
        System.out.println("Chosen plan was more expensive: " + (chosenCost > fullScanCost));
    }
}
