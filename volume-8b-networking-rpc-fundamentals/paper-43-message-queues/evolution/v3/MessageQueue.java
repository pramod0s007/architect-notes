import java.util.LinkedList;
import java.util.Queue;

/**
 * A message queue with at-least-once semantics: a message is only removed
 * from the queue by an explicit ack() call, never as a side effect of
 * reading it. peek() can be called any number of times without losing the
 * message -- if the consumer crashes before acking, the message is still
 * there on the next attempt.
 */
public class MessageQueue {
    private final Queue<String> messages = new LinkedList<>();

    public void enqueue(String message) {
        messages.add(message);
    }

    public String peek() {
        return messages.peek();
    }

    public void ack(String message) {
        messages.remove(message);
    }

    public int size() {
        return messages.size();
    }
}
