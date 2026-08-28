import java.util.HashSet;
import java.util.Set;

class IdempotentShippingEventHandler {
    private final ShippingService shipping;
    private final Set<String> processedEventIds = new HashSet<>();

    IdempotentShippingEventHandler(ShippingService shipping) {
        this.shipping = shipping;
    }

    String handle(OrderEvent event) {
        if (processedEventIds.contains(event.getEventId())) {
            return "SKIPPED (duplicate of " + event.getEventId() + ")";
        }
        processedEventIds.add(event.getEventId());
        return shipping.schedule(event.getOrder());
    }
}
