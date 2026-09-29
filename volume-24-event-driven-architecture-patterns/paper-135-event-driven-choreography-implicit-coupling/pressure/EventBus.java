import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal choreography-style event bus: listeners subscribe to an event type and are
 * invoked, synchronously, in the exact order they were registered, whenever an event of
 * that type is published. There is no central coordinator deciding what runs before what
 * -- execution order for a given event is entirely a function of registration order.
 */
public class EventBus {
    private final Map<String, List<Listener>> listeners = new HashMap<>();

    public void register(String eventType, Listener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public void publish(Event event) {
        for (Listener listener : listeners.getOrDefault(event.getType(), new ArrayList<>())) {
            listener.onEvent(event);
        }
    }
}
