import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test implementation: in-memory ArrayList store.
 * No files, no network — fast and deterministic in tests.
 */
public class InMemoryDataStore implements DataStore {

    private final List<String> records = new ArrayList<>();

    @Override
    public List<String> fetchRecords(String query) {
        return records.stream()
                .filter(r -> r.contains(query))
                .collect(Collectors.toList());
    }

    @Override
    public void saveRecord(String record) {
        records.add(record);
    }

    public int size() {
        return records.size();
    }
}
