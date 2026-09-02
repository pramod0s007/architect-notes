import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Same true database history as the pressure case: 5 real writes.
        List<ChangeEvent> trueHistory = new ArrayList<>();
        trueHistory.add(new ChangeEvent("U1", "PENDING",   1, false));
        trueHistory.add(new ChangeEvent("U1", "ACTIVE",    2, false));
        trueHistory.add(new ChangeEvent("U1", "SUSPENDED", 3, false));
        trueHistory.add(new ChangeEvent("U2", "PENDING",   1, false));
        trueHistory.add(new ChangeEvent("U2", null,        4, true)); // delete

        // Log-based CDC tails the transaction log itself, not the table's
        // current state -- every operation the log records becomes a
        // captured event, in the exact order it happened, deletes included.
        List<ChangeEvent> captured = new ArrayList<>(trueHistory);

        System.out.println("True database history: " + trueHistory.size() + " operations.");
        System.out.println("Log-tailing CDC captured " + captured.size() + " change event(s):");
        for (ChangeEvent e : captured) {
            System.out.println("  " + e.id + " -> " + (e.deleted ? "DELETED" : e.status) + " (at=" + e.updatedAt + ")");
        }

        long u1DistinctStates = captured.stream().filter(e -> e.id.equals("U1")).map(e -> e.status).distinct().count();
        boolean u2DeleteCaptured = captured.stream().anyMatch(e -> e.id.equals("U2") && e.deleted);

        System.out.println();
        System.out.println(captured.size() == trueHistory.size() && u1DistinctStates == 3 && u2DeleteCaptured
            ? "FIXED: every operation is captured exactly once, in order -- U1's full PENDING -> ACTIVE "
              + "-> SUSPENDED transition history is intact, and U2's delete is captured as an explicit "
              + "tombstone event instead of just silently disappearing from a query result."
            : "Unexpected -- investigate.");
    }
}
