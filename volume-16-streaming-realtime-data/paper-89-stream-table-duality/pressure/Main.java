import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // The authoritative stream: 5 real events, in true offset order.
        List<BalanceEvent> stream = new ArrayList<>();
        stream.add(new BalanceEvent("A", 0, 1000));
        stream.add(new BalanceEvent("B", 1, 2000));
        stream.add(new BalanceEvent("A", 2, 1500));
        stream.add(new BalanceEvent("B", 3, 2100));
        stream.add(new BalanceEvent("A", 4, 1200));

        // The "live" table is updated via a SEPARATE, independent write path --
        // a dual write alongside the stream append, not derived from it. One
        // of those independent writes (offset 4, A's final update) fails --
        // e.g., a timeout on the table's own write path -- while the stream
        // append for that same event still succeeds.
        Map<String, Integer> liveTable = new HashMap<>();
        for (BalanceEvent e : stream) {
            boolean directWriteFails = (e.offset == 4);
            if (!directWriteFails) {
                liveTable.put(e.accountId, e.newBalanceCents);
            }
        }

        // The stream itself is complete and authoritative -- folding it from
        // scratch reconstructs what should be true, independent of whatever
        // the live table's separate write path did or didn't do.
        Map<String, Integer> replayedTable = new HashMap<>();
        for (BalanceEvent e : stream) {
            replayedTable.put(e.accountId, e.newBalanceCents);
        }

        System.out.println("Live table (built via a separate direct-write path): A=" + liveTable.get("A")
            + "c, B=" + liveTable.get("B") + "c");
        System.out.println("Replayed table (folded fresh from the stream):       A=" + replayedTable.get("A")
            + "c, B=" + replayedTable.get("B") + "c");

        boolean drifted = !liveTable.equals(replayedTable);
        System.out.println();
        System.out.println(drifted
            ? "CONFIRMED FAILURE: the live table's balance for A (" + liveTable.get("A")
              + "c) does not match what the stream itself says happened (" + replayedTable.get("A")
              + "c). The stream recorded A's final update at offset 4 correctly -- the table's own, "
              + "independent write for that same event silently failed, and nothing links the two write "
              + "paths together, so the table has no way to know it fell behind."
            : "Unexpected -- investigate.");
    }
}
