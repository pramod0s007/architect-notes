import java.util.ArrayList;
import java.util.List;

// Looks like the same fix -- messages get inserted at a computed position
// instead of blind-appended. The position is computed from each SENDER
// DEVICE's own clock instead of a server-assigned sequence number.
class TimestampMessageInbox {
    private final List<TimestampedMessage> messages = new ArrayList<>();

    void receive(TimestampedMessage m) {
        int insertAt = 0;
        while (insertAt < messages.size() && messages.get(insertAt).clientTimestamp < m.clientTimestamp) {
            insertAt++;
        }
        messages.add(insertAt, m);
    }

    List<TimestampedMessage> messages() {
        return messages;
    }
}
