import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    static final int WINDOW_SIZE = 60;
    static final int ALLOWED_LATENESS = 10; // watermark = max event time seen so far - this

    public static void main(String[] args) {
        // Identical stream to the pressure case, processed in ARRIVAL order --
        // that's the only order a real stream processor ever sees events in.
        List<PurchaseEvent> events = new ArrayList<>();
        events.add(new PurchaseEvent(10, 10, 500));
        events.add(new PurchaseEvent(30, 30, 300));
        events.add(new PurchaseEvent(55, 65, 400)); // late arrival, but within allowed lateness
        events.add(new PurchaseEvent(70, 70, 600));

        Map<Integer, Integer> windowSums = new HashMap<>();
        Set<Integer> emittedWindows = new HashSet<>();
        List<String> emitLog = new ArrayList<>();
        int maxEventTimeSeen = 0;
        int droppedCents = 0;

        for (PurchaseEvent e : events) {
            // Bucket by EVENT time, not arrival time -- e3's late arrival still
            // lands in window 0, where it actually belongs.
            int window = e.eventTime / WINDOW_SIZE;

            maxEventTimeSeen = Math.max(maxEventTimeSeen, e.eventTime);
            int watermark = maxEventTimeSeen - ALLOWED_LATENESS;

            if (emittedWindows.contains(window)) {
                droppedCents += e.amountCents; // arrived beyond the allowed lateness -- too late
                continue;
            }
            windowSums.merge(window, e.amountCents, Integer::sum);

            // A window closes and emits once the watermark has passed its end --
            // meaning no event within the allowed lateness could still belong to it.
            for (int w = 0; w * WINDOW_SIZE + WINDOW_SIZE <= watermark; w++) {
                if (!emittedWindows.contains(w) && windowSums.containsKey(w)) {
                    emitLog.add("window " + w + " emitted: " + windowSums.get(w) + "c (watermark=" + watermark + ")");
                    emittedWindows.add(w);
                }
            }
        }
        // End of stream: flush any windows that never got emitted by watermark alone.
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
        System.out.println("Final: window 0 = " + windowSums.get(0) + "c, window 1 = " + windowSums.get(1)
            + "c, dropped = " + droppedCents + "c");

        System.out.println(windowSums.get(0) == 1200 && windowSums.get(1) == 600 && droppedCents == 0
            ? "FIXED: window 0's total (1200c) correctly includes the late-arriving purchase, because the "
              + "watermark only advances based on event-time progress, not wall-clock time -- window 0 "
              + "wasn't emitted until an event with eventTime=70 proved no more window-0 data (within the "
              + "allowed lateness) could still be coming."
            : "Unexpected -- investigate.");
    }
}
