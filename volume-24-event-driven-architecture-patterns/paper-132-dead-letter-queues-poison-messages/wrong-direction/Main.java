import java.util.ArrayDeque;
import java.util.Deque;

/**
 * WRONG DIRECTION: retry a bounded number of times, then silently DROP the message
 * and move on. This unblocks the queue -- msg-2 and msg-3 do get processed -- but
 * msg-1's data is now gone forever, with no record anywhere of what it was or that
 * it ever existed. Nobody can investigate why it failed or replay it later.
 */
public class Main {
    public static void main(String[] args) {
        Deque<Message> queue = new ArrayDeque<>();
        queue.add(new Message("msg-1", true));   // poison: always fails
        queue.add(new Message("msg-2", false));  // healthy
        queue.add(new Message("msg-3", false));  // healthy

        int processedCount = 0;
        int droppedCount = 0;
        final int retryLimit = 3;

        while (!queue.isEmpty()) {
            Message head = queue.pollFirst();
            int attempts = 0;
            boolean succeeded = false;
            while (attempts < retryLimit && !succeeded) {
                attempts++;
                try {
                    head.process();
                    succeeded = true;
                } catch (RuntimeException e) {
                    System.out.println("Retry " + attempts + " for " + head.getId() + " failed: " + e.getMessage());
                }
            }
            if (succeeded) {
                System.out.println("Processed " + head.getId());
                processedCount++;
            } else {
                System.out.println("Giving up on " + head.getId() + " after " + retryLimit
                        + " attempts -- dropping it silently, no record kept");
                droppedCount++;
            }
        }

        System.out.println();
        System.out.println("Messages processed: " + processedCount + " (expected 2: msg-2, msg-3)");
        System.out.println("Messages silently dropped: " + droppedCount + " (msg-1's data is now gone forever)");
    }
}
