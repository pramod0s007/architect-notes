import java.util.List;

/**
 * High-level business logic: builds sales reports.
 *
 * DIP: depends on DataStore abstraction. Works identically against
 * CSV files in production and an in-memory store in tests.
 */
public class SalesReportService {

    private final DataStore dataStore;

    public SalesReportService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public String generateDailySummary(String date) {
        List<String> records = dataStore.fetchRecords(date);
        if (records.isEmpty()) {
            return "No sales recorded for " + date;
        }
        double total = records.stream()
                .mapToDouble(this::parseAmount)
                .sum();
        return String.format("Sales on %s: %d transactions, total $%.2f", date, records.size(), total);
    }

    public int countSalesAbove(double threshold) {
        List<String> all = dataStore.fetchRecords("");   // fetch all
        return (int) all.stream()
                .filter(r -> parseAmount(r) > threshold)
                .count();
    }

    private double parseAmount(String record) {
        try {
            String[] parts = record.split(",");
            return Double.parseDouble(parts[parts.length - 1].trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
