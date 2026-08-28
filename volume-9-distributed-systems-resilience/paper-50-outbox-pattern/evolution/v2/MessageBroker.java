import java.util.ArrayList;
import java.util.List;

class MessageBroker {
    private final List<String> receivedEvents = new ArrayList<>();
    private boolean down = false;

    void goDown() {
        down = true;
    }

    void comeBackUp() {
        down = false;
    }

    boolean publish(String eventType, String orderId) {
        if (down) {
            return false;
        }
        receivedEvents.add(eventType + ":" + orderId);
        return true;
    }

    List<String> receivedEvents() {
        return receivedEvents;
    }
}
