import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static final int WINDOW_SIZE = 60;

    public static void main(String[] args) {
        // 4 real purchases. e3 happened at eventTime=55 (inside window 0,
        // [0,60)) but its network arrival is delayed to arrivalTime=65 --
        // well after window 0's wall-clock boundary has already passed.
        List<PurchaseEvent> events = new ArrayList<>();
        events.add(new PurchaseEvent(10, 10, 500));  // window 0, on-time
        events.add(new PurchaseEvent(30, 30, 300));  // window 0, on-time
        events.add(new PurchaseEvent(55, 65, 400));  // window 0 by event time, but arrives late
        events.add(new PurchaseEvent(70, 70, 600));  // window 1, on-time

        // Ground truth: what each window's revenue actually is, by event time.
        Map<Integer, Integer> trueByEventTime = new HashMap<>();
        for (PurchaseEvent e : events) {
            int window = e.eventTime / WINDOW_SIZE;
            trueByEventTime.merge(window, e.amountCents, Integer::sum);
        }

        // The naive implementation buckets by ARRIVAL time instead of event
        // time -- it processes events as they show up and assigns them to
        // whatever window is "currently open" by wall clock.
        Map<Integer, Integer> bucketedByArrival = new HashMap<>();
        for (PurchaseEvent e : events) {
            int window = e.arrivalTime / WINDOW_SIZE;
            bucketedByArrival.merge(window, e.amountCents, Integer::sum);
        }

        System.out.println("True revenue by event time:      window 0 = " + trueByEventTime.getOrDefault(0, 0)
            + "c, window 1 = " + trueByEventTime.getOrDefault(1, 0) + "c");
        System.out.println("Bucketed by arrival time instead: window 0 = " + bucketedByArrival.getOrDefault(0, 0)
            + "c, window 1 = " + bucketedByArrival.getOrDefault(1, 0) + "c");

        boolean mismatch = !trueByEventTime.equals(bucketedByArrival);
        System.out.println();
        System.out.println(mismatch
            ? "CONFIRMED FAILURE: window 0's revenue is understated by "
              + (trueByEventTime.get(0) - bucketedByArrival.getOrDefault(0, 0))
              + "c and window 1's is overstated by the same amount -- purely because one purchase's "
              + "network arrival happened to land 5 seconds after the wall-clock minute boundary, even "
              + "though the purchase itself genuinely happened inside window 0."
            : "Unexpected -- investigate.");
    }
}
