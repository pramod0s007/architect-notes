// SRP: Data / Analytics Team owns this class. Only they change it.
public class FulfillmentRecorder {

    public void record(ShipmentOrder order) {
        System.out.println("[Analytics] fulfillment_complete"
                + " customer=" + order.getCustomerId()
                + " sku="      + order.getProductSku()
                + " qty="      + order.getQuantity());
    }
}
