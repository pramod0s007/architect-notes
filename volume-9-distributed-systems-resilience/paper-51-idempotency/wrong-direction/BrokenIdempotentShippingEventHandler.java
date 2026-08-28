import java.util.HashSet;
import java.util.Set;

// Wrong direction: has dedup code, but the "key" isn't the event's own
// identity -- it's generated fresh on every call, so it never matches
// anything already in the set. Looks like idempotency. Isn't.
class BrokenIdempotentShippingEventHandler {
    private final ShippingService shipping;
    private final Set<String> processedKeys = new HashSet<>();
    private int callCount = 0;

    BrokenIdempotentShippingEventHandler(ShippingService shipping) {
        this.shipping = shipping;
    }

    String handle(OrderEvent event) {
        callCount++;
        String key = event.getOrder().getId() + ":" + callCount; // different every call
        if (processedKeys.contains(key)) {
            return "SKIPPED (duplicate of " + key + ")";
        }
        processedKeys.add(key);
        return shipping.schedule(event.getOrder());
    }
}
