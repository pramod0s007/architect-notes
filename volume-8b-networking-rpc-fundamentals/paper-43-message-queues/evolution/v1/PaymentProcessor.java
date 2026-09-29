/**
 * Stands in for a downstream payment service, called directly and
 * synchronously. Simulates a real, temporary outage: the second charge
 * request throws, as if the service was unreachable for a moment.
 */
public class PaymentProcessor {
    public void charge(String orderId, double amount) {
        if (orderId.equals("order-2")) {
            throw new RuntimeException("payment service unreachable");
        }
        System.out.println("Charged " + orderId + " for $" + amount);
    }
}
