// SRP: Customer Experience Team owns this class. Only they change it.
public class FulfillmentNotifier {

    public void notify(ShipmentOrder order, String trackingId) {
        String subject = "Your order " + order.getId() + " is on its way!";
        System.out.println("[Email] → " + order.getCustomerEmail()
                + " | subject=" + subject
                + " | tracking=" + trackingId);
    }
}
