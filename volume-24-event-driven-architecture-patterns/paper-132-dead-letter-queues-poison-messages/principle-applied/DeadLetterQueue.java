import java.util.ArrayList;
import java.util.List;

/**
 * A separate, durable store for messages that exhausted their retry limit. Nothing is
 * ever discarded -- a message that ends up here is preserved for inspection or replay,
 * distinguishing "we gave up trying automatically" from "the data is gone."
 */
public class DeadLetterQueue {
    private final List<Message> deadLetters = new ArrayList<>();

    public void add(Message m) {
        deadLetters.add(m);
    }

    public int size() {
        return deadLetters.size();
    }

    public List<Message> getAll() {
        return deadLetters;
    }
}
