// SRP: Logistics Team owns this class. Only they change it.
public class ShipmentDispatcher {

    public String dispatch(ShipmentOrder order) {
        if (order.getDestinationAddress() == null || order.getDestinationAddress().isBlank()) {
            throw new IllegalArgumentException("Destination address required");
        }
        String trackingId = "TRK-" + order.getId().toUpperCase();
        System.out.println("[Shipping] Dispatched to " + order.getDestinationAddress()
                + " | tracking=" + trackingId);
        return trackingId;
    }
}
