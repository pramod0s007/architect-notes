import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production implementation: reads from a CSV file (simulated with hardcoded rows).
 * The query string is matched against the date field in each record.
 */
public class CsvDataStore implements DataStore {

    // Format: "date,product,amount"
    private static final List<String> CSV_ROWS = Arrays.asList(
        "2024-01-15,Widget A,250.00",
        "2024-01-15,Widget B,89.99",
        "2024-01-15,Gadget Pro,1200.00",
        "2024-01-16,Widget A,310.00",
        "2024-01-16,Gadget Pro,950.00"
    );

    @Override
    public List<String> fetchRecords(String query) {
        System.out.println("[CSV] Reading records matching: " + query);
        return CSV_ROWS.stream()
                .filter(row -> row.contains(query))
                .collect(Collectors.toList());
    }

    @Override
    public void saveRecord(String record) {
        System.out.println("[CSV] Appending to file: " + record);
    }
}
