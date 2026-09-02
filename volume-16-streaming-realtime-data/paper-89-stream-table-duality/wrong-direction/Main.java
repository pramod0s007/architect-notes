import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // The same authoritative stream, in its TRUE offset order.
        List<BalanceEvent> stream = new ArrayList<>();
        stream.add(new BalanceEvent("A", 0, 1000));
        stream.add(new BalanceEvent("B", 1, 2000));
        stream.add(new BalanceEvent("A", 2, 1500));
        stream.add(new BalanceEvent("B", 3, 2100));
        stream.add(new BalanceEvent("A", 4, 1200));

        // Overcorrection: the table is still derived ONLY from the stream --
        // no dual write, no second path -- so it avoids the pressure case's
        // mistake entirely. But a topic repartition mid-stream (a partition-count
        // change re-hashed and redistributed existing keys) transiently split A's
        // history across two partitions, and the fold processes events in the
        // order a multi-partition merge happened to DELIVER them, not true offset
        // order: A's offset-4 update (partition lagging) is merged in before
        // A's offset-2 update (a different partition, further ahead) is
        // flushed. B's two events happen to merge in their true order.
        List<BalanceEvent> mergedDeliveryOrder = new ArrayList<>();
        mergedDeliveryOrder.add(stream.get(0)); // A, offset 0
        mergedDeliveryOrder.add(stream.get(1)); // B, offset 1
        mergedDeliveryOrder.add(stream.get(3)); // B, offset 3
        mergedDeliveryOrder.add(stream.get(4)); // A, offset 4 -- delivered before A's offset 2
        mergedDeliveryOrder.add(stream.get(2)); // A, offset 2 -- delivered last, overwrites offset 4's result

        Map<String, Integer> table = new HashMap<>();
        for (BalanceEvent e : mergedDeliveryOrder) {
            table.put(e.accountId, e.newBalanceCents); // last MERGED wins, not last true offset
        }

        // What the table SHOULD be if folded in true offset order.
        Map<String, Integer> correctTable = new HashMap<>();
        for (BalanceEvent e : stream) {
            correctTable.put(e.accountId, e.newBalanceCents);
        }

        System.out.println("Table folded in merge-delivery order: A=" + table.get("A") + "c, B=" + table.get("B") + "c");
        System.out.println("Table folded in true offset order:    A=" + correctTable.get("A") + "c, B=" + correctTable.get("B") + "c");

        boolean drifted = !table.equals(correctTable);
        System.out.println();
        System.out.println(drifted
            ? "CONFIRMED FAILURE: A's table value is " + table.get("A") + "c, but the stream's true, "
              + "offset-ordered final value for A is " + correctTable.get("A") + "c. The table was built "
              + "with no dual write and no separate source at all -- purely folded from the stream -- and "
              + "it still drifted, because the fold applied A's offset-2 event AFTER A's offset-4 event, "
              + "reversing their true order. Deriving a table from a stream is necessary but not "
              + "sufficient; the fold has to preserve each key's own offset order, or 'derived from the "
              + "stream' stops meaning what it sounds like it means."
            : "Unexpected -- investigate.");
    }
}
