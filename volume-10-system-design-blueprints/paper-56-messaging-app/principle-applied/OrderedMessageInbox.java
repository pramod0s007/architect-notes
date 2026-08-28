import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Inserts each message at the position its server-assigned sequence number
// dictates, and ignores anything already seen -- delivery order and
// duplication become irrelevant to what the user sees.
class OrderedMessageInbox {
    private final List<Message> messages = new ArrayList<>();
    private final Set<Integer> seenSequenceNumbers = new HashSet<>();

    void receive(Message m) {
        if (!seenSequenceNumbers.add(m.sequenceNumber)) {
            return; // duplicate delivery of a message we already have
        }
        int insertAt = 0;
        while (insertAt < messages.size() && messages.get(insertAt).sequenceNumber < m.sequenceNumber) {
            insertAt++;
        }
        messages.add(insertAt, m);
    }

    List<Message> messages() {
        return messages;
    }
}
