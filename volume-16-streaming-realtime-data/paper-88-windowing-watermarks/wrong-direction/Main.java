import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    static final int WINDOW_SIZE = 60;

    public static void main(String[] args) {
        // Identical stream, identical event-time bucketing key as the fix --
        // the overcorrection is entirely in how the watermark advances.
        List<PurchaseEvent> events = new ArrayList<>();
        events.add(new PurchaseEvent(10, 10, 500));
        events.add(new PurchaseEvent(30, 30, 300));
        events.add(new PurchaseEvent(55, 65, 400)); // late arrival -- correctly keyed to window 0
        events.add(new PurchaseEvent(70, 70, 600));

        Map<Integer, Integer> windowSums = new HashMap<>();
        Set<Integer> emittedWindows = new HashSet<>();
        List<String> emitLog = new ArrayList<>();
        int watermark = 0;
        int droppedCents = 0;

        for (PurchaseEvent e : events) {
            // Overcorrection: the watermark tracks ARRIVAL (wall-clock) time
            // directly, with zero lateness allowance, instead of tracking
            // event-time progress. It advances the instant a late event's
            // own arrival crosses a window boundary -- closing that window
            // before the late event itself gets a chance to be counted.
            watermark = Math.max(watermark, e.arrivalTime);

            for (int w = 0; w * WINDOW_SIZE + WINDOW_SIZE <= watermark; w++) {
                if (!emittedWindows.contains(w)) {
                    int total = windowSums.getOrDefault(w, 0);
                    emitLog.add("window " + w + " emitted: " + total + "c (watermark=" + watermark + ")");
                    emittedWindows.add(w);
                }
            }

            int window = e.eventTime / WINDOW_SIZE; // correct key -- doesn't help once the window is closed
            if (emittedWindows.contains(window)) {
                droppedCents += e.amountCents;
                emitLog.add("  -> DROPPED: eventTime=" + e.eventTime + " belongs to already-closed window " + window);
                continue;
            }
            windowSums.merge(window, e.amountCents, Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entry : windowSums.entrySet()) {
            if (!emittedWindows.contains(entry.getKey())) {
                emitLog.add("window " + entry.getKey() + " flushed at end of stream: " + entry.getValue() + "c");
                emittedWindows.add(entry.getKey());
            }
        }

        for (String line : emitLog) {
            System.out.println("  " + line);
        }
        System.out.println();
        System.out.println("Final: window 0 = " + windowSums.getOrDefault(0, 0) + "c, window 1 = "
            + windowSums.getOrDefault(1, 0) + "c, dropped = " + droppedCents + "c");

        System.out.println(droppedCents > 0
            ? "CONFIRMED FAILURE: window 0 is missing " + droppedCents + "c. The event was correctly keyed "
              + "to window 0 by its true event time -- it was dropped anyway, because the watermark closed "
              + "window 0 based on wall-clock arrival progress, not event-time progress, and it closed the "
              + "window in the same tick the late event arrived, one step too early to still count it."
            : "Unexpected -- investigate.");
    }
}
