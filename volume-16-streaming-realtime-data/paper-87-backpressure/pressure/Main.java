import java.util.LinkedList;
import java.util.Queue;

public class Main {
    static final int TOTAL_EVENTS = 12;
    static final int SAFE_QUEUE_LIMIT = 5; // the memory budget this queue is expected to stay under

    public static void main(String[] args) {
        // An unbounded queue between a fast producer (1 event/tick) and a slow
        // consumer (1 event every 3 ticks). The producer never checks queue
        // size before adding -- it just keeps producing at its own pace.
        Queue<Integer> queue = new LinkedList<>();
        int consumed = 0;
        int maxQueueSize = 0;

        for (int tick = 1; tick <= TOTAL_EVENTS; tick++) {
            queue.add(tick); // producer always adds, regardless of queue size
            maxQueueSize = Math.max(maxQueueSize, queue.size());
            if (tick % 3 == 0 && !queue.isEmpty()) {
                queue.poll();
                consumed++;
            }
        }

        System.out.println("Producer: 1 event/tick. Consumer: 1 event every 3 ticks. " + TOTAL_EVENTS + " ticks run.");
        System.out.println("Produced: " + TOTAL_EVENTS + ", Consumed: " + consumed
            + ", Final queue size: " + queue.size() + ", Peak queue size: " + maxQueueSize);
        System.out.println("Safe queue limit (memory budget): " + SAFE_QUEUE_LIMIT);

        System.out.println(maxQueueSize > SAFE_QUEUE_LIMIT
            ? "CONFIRMED FAILURE: peak queue size (" + maxQueueSize + ") exceeded the safe limit ("
              + SAFE_QUEUE_LIMIT + "). The mismatch between producer and consumer speed is permanent, "
              + "not temporary -- the queue doesn't recover, it just keeps growing every tick the "
              + "producer runs, heading toward an out-of-memory failure with no upper bound."
            : "Unexpected -- investigate.");
    }
}
