/**
 * DIP — report-data-service
 *
 * SalesReportService is unchanged whether backed by CSV (production) or
 * an in-memory store (test). Data-store swappability demonstrated below.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Production: CSV-backed store ===");
        DataStore csv = new CsvDataStore();
        SalesReportService productionService = new SalesReportService(csv);
        System.out.println(productionService.generateDailySummary("2024-01-15"));
        System.out.println(productionService.generateDailySummary("2024-01-16"));
        System.out.println("Sales above $500: " + productionService.countSalesAbove(500.0));

        System.out.println();
        System.out.println("=== Test: in-memory store ===");
        InMemoryDataStore mem = new InMemoryDataStore();
        mem.saveRecord("2024-03-01,Test Product,750.00");
        mem.saveRecord("2024-03-01,Cheap Item,15.00");
        SalesReportService testService = new SalesReportService(mem);
        System.out.println(testService.generateDailySummary("2024-03-01"));
        System.out.println("Sales above $500: " + testService.countSalesAbove(500.0));

        System.out.println();
        System.out.println("SalesReportService source code: identical in both scenarios.");
    }
}
