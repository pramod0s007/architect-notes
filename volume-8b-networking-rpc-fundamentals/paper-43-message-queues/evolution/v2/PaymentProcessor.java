/**
 * Stands in for a downstream payment service. Charging order-2 throws, as
 * if the consumer process crashed partway through handling that message --
 * after the message was already removed from the queue.
 */
public class PaymentProcessor {
    public void charge(String orderId, double amount) {
        if (orderId.equals("order-2")) {
            throw new RuntimeException("consumer crashed mid-processing");
        }
        System.out.println("Charged " + orderId + " for $" + amount);
    }
}
