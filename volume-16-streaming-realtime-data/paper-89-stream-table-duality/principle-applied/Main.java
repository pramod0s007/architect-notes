import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static Map<String, Integer> foldStream(List<BalanceEvent> stream) {
        // The table has exactly one way to come into existence: fold the
        // stream, in offset order, applying each event's SET. There is no
        // second write path that could ever fall out of sync with this one,
        // because there is no other path at all.
        Map<String, Integer> table = new HashMap<>();
        for (BalanceEvent e : stream) {
            table.put(e.accountId, e.newBalanceCents);
        }
        return table;
    }

    public static void main(String[] args) {
        // The same authoritative stream as the pressure case.
        List<BalanceEvent> stream = new ArrayList<>();
        stream.add(new BalanceEvent("A", 0, 1000));
        stream.add(new BalanceEvent("B", 1, 2000));
        stream.add(new BalanceEvent("A", 2, 1500));
        stream.add(new BalanceEvent("B", 3, 2100));
        stream.add(new BalanceEvent("A", 4, 1200));

        Map<String, Integer> table = foldStream(stream);
        System.out.println("Table (folded from the stream): A=" + table.get("A") + "c, B=" + table.get("B") + "c");

        // Simulate the table's in-memory state being lost entirely --
        // a crash, a restart, a cold cache -- and rebuild it from nothing but
        // the stream, exactly as it was built the first time.
        Map<String, Integer> rebuiltAfterCrash = foldStream(stream);
        System.out.println("Table rebuilt from scratch after a simulated crash: A=" + rebuiltAfterCrash.get("A")
            + "c, B=" + rebuiltAfterCrash.get("B") + "c");

        boolean identical = table.equals(rebuiltAfterCrash);
        System.out.println();
        System.out.println(identical
            ? "FIXED: the table before the crash and the table rebuilt after it are identical, because "
              + "both are nothing more than the same fold of the same stream. There was never a second "
              + "write path to lose sync with -- the table IS the stream, viewed as current state instead "
              + "of as a log. Recomputing it from the log always reproduces the exact same result."
            : "Unexpected -- investigate.");
    }
}
