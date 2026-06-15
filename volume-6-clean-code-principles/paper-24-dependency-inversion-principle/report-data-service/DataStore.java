import java.util.List;

/**
 * DIP: abstraction owned by the business-logic layer.
 * SalesReportService depends on this — never on CSV files or databases.
 */
public interface DataStore {

    List<String> fetchRecords(String query);

    void saveRecord(String record);
}
