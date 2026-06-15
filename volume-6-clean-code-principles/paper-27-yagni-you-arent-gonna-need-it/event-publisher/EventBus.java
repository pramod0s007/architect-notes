import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * V2: EventBus introduced when the second handler (AuditHandler) arrived.
 *
 * YAGNI applied correctly: the bus was built to solve a real, present problem
 * (two handlers for the same event) — not speculatively during v1.
 * No frameworks, no annotations, no reflection — 25 lines that do the job.
 */
public class EventBus {

    private final Map<String, List<Consumer<OrderEvent>>> subscribers = new HashMap<>();

    public void register(String eventType, Consumer<OrderEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    public void publish(String eventType, OrderEvent event) {
        List<Consumer<OrderEvent>> handlers = subscribers.getOrDefault(eventType, List.of());
        System.out.println("[EventBus] Publishing " + eventType + " to " + handlers.size() + " handlers");
        handlers.forEach(h -> h.accept(event));
    }
}
