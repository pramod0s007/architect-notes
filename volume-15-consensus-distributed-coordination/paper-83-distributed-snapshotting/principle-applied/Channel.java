import java.util.ArrayList;
import java.util.List;

// A FIFO channel between two nodes. Messages arrive in exactly the order they
// were sent -- the property the marker-based snapshot algorithm depends on.
class Channel {
    private final List<String> messages = new ArrayList<>();

    void send(String message) {
        messages.add(message);
    }

    String deliverNext() {
        return messages.isEmpty() ? null : messages.remove(0);
    }
}
