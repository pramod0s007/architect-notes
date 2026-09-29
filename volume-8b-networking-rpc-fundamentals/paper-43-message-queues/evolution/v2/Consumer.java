/**
 * Drains the queue one message at a time. Each message is already removed
 * from the queue the instant dequeue() returns it -- so if processing that
 * message crashes, there is no copy of it left anywhere to retry.
 */
public class Consumer {
    private final MessageQueue queue;
    private final PaymentProcessor paymentProcessor;

    public Consumer(MessageQueue queue, PaymentProcessor paymentProcessor) {
        this.queue = queue;
        this.paymentProcessor = paymentProcessor;
    }

    public void processAll() {
        String message;
        while ((message = queue.dequeue()) != null) {
            String[] parts = message.split(":");
            String orderId = parts[0];
            double amount = Double.parseDouble(parts[1]);
            try {
                paymentProcessor.charge(orderId, amount);
            } catch (RuntimeException e) {
                System.out.println("Processing " + orderId + " crashed: " + e.getMessage()
                        + " -- but it was already removed from the queue, so it's gone for good");
            }
        }
    }
}
