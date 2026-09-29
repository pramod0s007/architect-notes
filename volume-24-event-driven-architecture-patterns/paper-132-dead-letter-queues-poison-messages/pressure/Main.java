import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PRESSURE: naive infinite-retry consumer. On failure it retries the SAME message
 * again instead of moving on, so a single poison message blocks the entire queue
 * forever -- every other message behind it never gets a chance to be processed.
 * Bounded to 5 retries here only so the demo terminates and prints a clear result;
 * the real failure is that it never advances past msg-1 regardless of how many
 * retries are allowed.
 */
public class Main {
    public static void main(String[] args) {
        Deque<Message> queue = new ArrayDeque<>();
        queue.add(new Message("msg-1", true));   // poison: always fails
        queue.add(new Message("msg-2", false));  // healthy
        queue.add(new Message("msg-3", false));  // healthy

        int processedCount = 0;
        int retriesOnHead = 0;
        final int demoRetryCap = 5; // only to make the demo terminate and print a result

        while (!queue.isEmpty() && retriesOnHead < demoRetryCap) {
            Message head = queue.peekFirst();
            try {
                head.process();
                System.out.println("Processed " + head.getId());
                queue.pollFirst();
                processedCount++;
                retriesOnHead = 0;
            } catch (RuntimeException e) {
                retriesOnHead++;
                System.out.println("Retry " + retriesOnHead + " for " + head.getId() + " failed: " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("Messages processed: " + processedCount + " (expected 3 if the queue could advance)");
        System.out.println("Messages remaining in queue (msg-1 itself plus everything behind it): " + queue.size());
        System.out.println("Retries spent on msg-1 alone: " + retriesOnHead
                + " (would keep growing forever without the demo cap)");
    }
}
