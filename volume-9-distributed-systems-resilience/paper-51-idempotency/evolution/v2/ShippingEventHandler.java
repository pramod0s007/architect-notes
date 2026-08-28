// At-least-once delivery (Paper 50) means this can be called more than once
// for the SAME event. Nothing here checks whether that already happened.
class ShippingEventHandler {
    private final ShippingService shipping;

    ShippingEventHandler(ShippingService shipping) {
        this.shipping = shipping;
    }

    void handle(OrderEvent event) {
        shipping.schedule(event.getOrder());
    }
}
