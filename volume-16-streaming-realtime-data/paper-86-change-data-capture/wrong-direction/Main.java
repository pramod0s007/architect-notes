import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Same true database history as the other two stages: 5 real writes.
        List<ChangeEvent> trueHistory = new ArrayList<>();
        trueHistory.add(new ChangeEvent("U1", "PENDING",   1, false));
        trueHistory.add(new ChangeEvent("U1", "ACTIVE",    2, false));
        trueHistory.add(new ChangeEvent("U1", "SUSPENDED", 3, false));
        trueHistory.add(new ChangeEvent("U2", "PENDING",   1, false));
        trueHistory.add(new ChangeEvent("U2", null,        4, true)); // delete

        // Overcorrection: still tail the log (not the polling mistake from
        // the pressure case), but COMPACT by key before publishing, keeping
        // only the latest event per key -- a common throughput optimization
        // for consumers that only care about "current state."
        // NOTE: LinkedHashMap preserves each key's FIRST-occurrence position, not the
        // surviving record's offset -- fine for this dataset (each key's writes are
        // contiguous), but a real compacted log orders survivors by their own offset,
        // not their key's first appearance.
        Map<String, ChangeEvent> compactedByKey = new LinkedHashMap<>();
        for (ChangeEvent e : trueHistory) {
            compactedByKey.put(e.id, e); // later event for the same key overwrites the earlier one
        }
        List<ChangeEvent> captured = new ArrayList<>(compactedByKey.values());

        System.out.println("True database history: " + trueHistory.size() + " operations.");
        System.out.println("Log-tailing CDC with per-key compaction captured " + captured.size() + " change event(s):");
        for (ChangeEvent e : captured) {
            System.out.println("  " + e.id + " -> " + (e.deleted ? "DELETED" : e.status) + " (at=" + e.updatedAt + ")");
        }

        boolean u2DeleteCaptured = captured.stream().anyMatch(e -> e.id.equals("U2") && e.deleted);
        long u1DistinctStatesCaptured = captured.stream().filter(e -> e.id.equals("U1")).map(e -> e.status).distinct().count();

        System.out.println();
        System.out.println(u2DeleteCaptured && u1DistinctStatesCaptured == 1
            ? "CONFIRMED FAILURE: U2's delete IS captured correctly -- compaction naturally keeps the "
              + "last event per key, and a delete is always the last event for a deleted row. But U1's "
              + "intermediate ACTIVE state, sandwiched between two other writes to the same key within "
              + "this batch, is compacted away just as completely as it was in the naive polling case. "
              + "A consumer counting status transitions per user sees 1 event for U1 instead of the 2 "
              + "real transitions that happened -- compaction is the right call for a consumer that only "
              + "wants current state, and silently wrong for one that needs full history."
            : "Unexpected -- investigate.");
    }
}
