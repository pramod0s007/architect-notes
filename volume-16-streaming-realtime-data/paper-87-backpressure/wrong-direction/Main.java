import java.util.LinkedList;
import java.util.Queue;

public class Main {
    static final int TOTAL_EVENTS = 12;
    static final int QUEUE_CAPACITY = 5;

    public static void main(String[] args) {
        // Overcorrection: bounded queue, same capacity as the fix -- but
        // instead of blocking the producer when full, silently DROP the new
        // event and let the producer keep running at full speed, never
        // slowed down. Consumer is still 1 event/3 ticks.
        Queue<Integer> queue = new LinkedList<>();
        int consumed = 0;
        int dropped = 0;
        int maxQueueSize = 0;

        for (int tick = 1; tick <= TOTAL_EVENTS; tick++) {
            if (queue.size() < QUEUE_CAPACITY) {
                queue.add(tick);
            } else {
                dropped++; // the event is gone forever -- not retried, not queued anywhere
            }
            maxQueueSize = Math.max(maxQueueSize, queue.size());
            if (tick % 3 == 0 && !queue.isEmpty()) {
                queue.poll();
                consumed++;
            }
        }

        System.out.println("Producer: 1 event/tick, never blocks. Consumer: 1 event every 3 ticks.");
        System.out.println("Produced (attempted): " + TOTAL_EVENTS + ", Consumed: " + consumed
            + ", Dropped: " + dropped + ", Still queued: " + queue.size() + ", Peak queue size: " + maxQueueSize);

        System.out.println(dropped > 0
            ? "CONFIRMED FAILURE: " + dropped + " of " + TOTAL_EVENTS + " real events were permanently "
              + "lost -- not delayed, not retried, gone. The queue itself never exceeded its capacity "
              + "of " + QUEUE_CAPACITY + " and the producer never stalled, which is exactly the "
              + "throughput profile the fix was trying to achieve -- except the fix achieved it by "
              + "slowing the producer down, and this achieved it by deleting the producer's data."
            : "Unexpected -- investigate.");
    }
}
