import java.util.LinkedList;
import java.util.Queue;

/**
 * A simple in-memory message queue with at-most-once semantics: dequeue()
 * removes and returns a message in a single step, before the consumer has
 * done anything with it. If the consumer crashes while handling a message
 * it already dequeued, that message is gone -- the queue has no idea it
 * was never actually finished.
 */
public class MessageQueue {
    private final Queue<String> messages = new LinkedList<>();

    public void enqueue(String message) {
        messages.add(message);
    }

    public String dequeue() {
        return messages.poll();
    }

    public int size() {
        return messages.size();
    }
}
