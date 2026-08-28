import java.util.ArrayList;
import java.util.List;

// Fails the first N publish attempts, then succeeds -- same shape as Paper 47's
// FlakyBackend, standing in for a broker that's briefly unreachable.
class FlakyMessageBroker {
    private final List<String> receivedEvents = new ArrayList<>();
    private int failuresRemaining;

    FlakyMessageBroker(int failuresRemaining) {
        this.failuresRemaining = failuresRemaining;
    }

    boolean publish(String eventType, String orderId) {
        if (failuresRemaining > 0) {
            failuresRemaining--;
            return false;
        }
        receivedEvents.add(eventType + ":" + orderId);
        return true;
    }

    List<String> receivedEvents() {
        return receivedEvents;
    }
}
