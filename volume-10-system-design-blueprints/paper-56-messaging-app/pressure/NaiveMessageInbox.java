import java.util.ArrayList;
import java.util.List;

// Displays messages in whatever order the network happened to deliver them.
// Correct only if delivery order always matches send order -- which
// at-least-once, multi-path delivery never guarantees.
class NaiveMessageInbox {
    private final List<Message> messages = new ArrayList<>();

    void receive(Message m) {
        messages.add(m);
    }

    List<Message> messages() {
        return messages;
    }
}
