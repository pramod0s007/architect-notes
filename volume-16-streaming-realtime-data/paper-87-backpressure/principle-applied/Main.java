import java.util.LinkedList;
import java.util.Queue;

public class Main {
    static final int TOTAL_EVENTS = 12;
    static final int QUEUE_CAPACITY = 5;
    static final int MAX_TICKS = 200; // generous upper bound so the loop always terminates

    public static void main(String[] args) {
        // A bounded queue: when it's full, the producer BLOCKS instead of
        // adding -- it retries the same event on the next tick until the
        // consumer has drained enough space. Consumer is still 1 event/3 ticks.
        Queue<Integer> queue = new LinkedList<>();
        int nextToProduce = 1;
        int consumed = 0;
        int stalledTicks = 0;
        int maxQueueSize = 0;
        int tick = 0;

        while ((nextToProduce <= TOTAL_EVENTS || !queue.isEmpty()) && tick < MAX_TICKS) {
            tick++;
            if (nextToProduce <= TOTAL_EVENTS) {
                if (queue.size() < QUEUE_CAPACITY) {
                    queue.add(nextToProduce);
                    nextToProduce++;
                } else {
                    stalledTicks++; // producer blocked this tick -- applies backpressure
                }
            }
            maxQueueSize = Math.max(maxQueueSize, queue.size());
            if (tick % 3 == 0 && !queue.isEmpty()) {
                queue.poll();
                consumed++;
            }
        }

        System.out.println("Producer: 1 event/tick, blocks when queue is full. Consumer: 1 event every 3 ticks.");
        System.out.println("Ticks to fully drain: " + tick + ", Producer stalled on " + stalledTicks + " tick(s).");
        System.out.println("Produced: " + (nextToProduce - 1) + ", Consumed: " + consumed
            + ", Final queue size: " + queue.size() + ", Peak queue size: " + maxQueueSize);

        System.out.println((nextToProduce - 1 == TOTAL_EVENTS) && consumed == TOTAL_EVENTS
                && queue.isEmpty() && maxQueueSize <= QUEUE_CAPACITY
            ? "FIXED: every one of the " + TOTAL_EVENTS + " events was eventually produced and consumed "
              + "-- zero data loss -- and the queue never exceeded its capacity of " + QUEUE_CAPACITY
              + ". The producer's effective throughput was throttled down to match the consumer's real "
              + "rate; it took longer than " + TOTAL_EVENTS + " ticks, but nothing was ever dropped."
            : "Unexpected -- investigate.");
    }
}
