import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // The database's true operation history -- 5 real writes across 2 rows.
        // U1 goes PENDING -> ACTIVE -> SUSPENDED. U2 is inserted, then deleted.
        List<ChangeEvent> trueHistory = new ArrayList<>();
        trueHistory.add(new ChangeEvent("U1", "PENDING",   1, false));
        trueHistory.add(new ChangeEvent("U1", "ACTIVE",    2, false));
        trueHistory.add(new ChangeEvent("U1", "SUSPENDED", 3, false));
        trueHistory.add(new ChangeEvent("U2", "PENDING",   1, false));
        trueHistory.add(new ChangeEvent("U2", null,        4, true)); // delete

        // A naive poller only ever queries CURRENT table state, not history.
        // Apply every op to build what the table actually looks like right now.
        Map<String, ChangeEvent> currentTable = new HashMap<>();
        for (ChangeEvent e : trueHistory) {
            if (e.deleted) {
                currentTable.remove(e.id);
            } else {
                currentTable.put(e.id, e);
            }
        }

        // Poll query: "SELECT * WHERE updatedAt > lastPollTime" -- lastPollTime=0,
        // so every row currently in the table matches.
        int lastPollTime = 0;
        List<ChangeEvent> captured = new ArrayList<>();
        for (ChangeEvent row : currentTable.values()) {
            if (row.updatedAt > lastPollTime) {
                captured.add(row);
            }
        }

        System.out.println("True database history: " + trueHistory.size() + " operations (3 writes to U1, insert+delete of U2).");
        System.out.println("Poll query captured " + captured.size() + " change event(s):");
        for (ChangeEvent e : captured) {
            System.out.println("  " + e.id + " -> " + e.status + " (updatedAt=" + e.updatedAt + ")");
        }

        boolean sawActiveTransition = captured.stream().anyMatch(e -> "ACTIVE".equals(e.status));
        boolean sawU2AtAll = currentTable.containsKey("U2") || captured.stream().anyMatch(e -> e.id.equals("U2"));

        System.out.println();
        System.out.println(captured.size() == 1 && !sawActiveTransition && !sawU2AtAll
            ? "CONFIRMED FAILURE: U1's PENDING->ACTIVE transition was never captured -- only the final "
              + "SUSPENDED state exists by poll time. U2's entire insert-then-delete lifecycle is "
              + "invisible: the row is gone from the table, so the poll query's WHERE clause has "
              + "nothing left to match, and no delete event is ever produced."
            : "Unexpected -- investigate.");
    }
}
