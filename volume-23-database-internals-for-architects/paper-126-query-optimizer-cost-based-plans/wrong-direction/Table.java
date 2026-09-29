import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A table of 1,000 rows with a heavily skewed "status" column (950
 * "active", 50 "inactive"), plus a real index mapping each status
 * value to the row positions that hold it. The index itself is
 * genuine data -- built once by scanning every row -- not a stand-in
 * or a hardcoded count.
 */
public class Table {
    public static final int TOTAL_ROWS = 1000;
    public static final int ACTIVE_ROWS = 950;
    public static final int INACTIVE_ROWS = 50;

    private final List<String> statusColumn = new ArrayList<>();
    private final Map<String, List<Integer>> statusIndex = new HashMap<>();

    public Table() {
        for (int i = 0; i < ACTIVE_ROWS; i++) {
            statusColumn.add("active");
        }
        for (int i = 0; i < INACTIVE_ROWS; i++) {
            statusColumn.add("inactive");
        }
        for (int position = 0; position < statusColumn.size(); position++) {
            statusIndex.computeIfAbsent(statusColumn.get(position), k -> new ArrayList<>()).add(position);
        }
    }

    public int totalRowCount() {
        return statusColumn.size();
    }

    /** The real row positions holding this status value, per the index -- no execution required to get this count. */
    public List<Integer> matchingPositions(String status) {
        return statusIndex.getOrDefault(status, new ArrayList<>());
    }
}
