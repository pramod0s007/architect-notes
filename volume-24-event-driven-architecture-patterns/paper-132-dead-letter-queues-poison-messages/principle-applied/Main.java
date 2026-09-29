import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PRINCIPLE APPLIED: retry a bounded number of times, then move the message to a
 * separate Dead Letter Queue instead of dropping it. The main queue unblocks exactly
 * like the wrong-direction version, but msg-1's data is preserved in the DLQ -- it can
 * be inspected, alerted on, or replayed later, instead of vanishing without a trace.
 */
public class Main {
    public static void main(String[] args) {
        Deque<Message> queue = new ArrayDeque<>();
        queue.add(new Message("msg-1", true));   // poison: always fails
        queue.add(new Message("msg-2", false));  // healthy
        queue.add(new Message("msg-3", false));  // healthy

        DeadLetterQueue dlq = new DeadLetterQueue();
        int processedCount = 0;
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
                dlq.add(head);
                System.out.println("Giving up on " + head.getId() + " after " + retryLimit
                        + " attempts -- moved to dead letter queue, not dropped");
            }
        }

        System.out.println();
        System.out.println("Messages processed: " + processedCount + " (expected 2: msg-2, msg-3)");
        System.out.println("Messages in dead letter queue: " + dlq.size() + " (expected 1: msg-1, preserved)");
        for (Message dead : dlq.getAll()) {
            System.out.println("  DLQ contains: " + dead.getId() + " -- available for inspection or replay");
        }
    }
}
