// SRP — owned by the Data Engineering team.
// Single responsibility: retrieve raw sales records for a period.
// Formatting or delivery concerns never touch this class.

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDataFetcher {

    // Simulates a DB/API call; in production inject a DataSource or HTTP client
    private static final Map<String, List<String>> DATA_STORE = new HashMap<>();

    static {
        DATA_STORE.put("2024-Q1", Arrays.asList(
                "ProductA,North,12500.00",
                "ProductB,North,8300.50",
                "ProductC,South,21000.75",
                "ProductA,South,9400.00",
                "ProductD,West,4750.25"
        ));
        DATA_STORE.put("2024-Q2", Arrays.asList(
                "ProductA,North,15200.00",
                "ProductB,East,6100.00",
                "ProductC,South,18900.50",
                "ProductE,West,31000.00"
        ));
    }

    public List<String> fetchSalesData(String period) {
        List<String> rows = DATA_STORE.get(period);
        if (rows == null || rows.isEmpty()) {
            System.out.println("[DataFetcher] No data found for period: " + period);
            return List.of();
        }
        System.out.println("[DataFetcher] Fetched " + rows.size()
                + " records for period " + period);
        return rows;
    }
}
