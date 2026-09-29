import java.util.HashSet;
import java.util.Set;

/**
 * Same at-least-once queue interaction as Consumer, but tracks which order
 * IDs have already been successfully charged. On redelivery of a message
 * whose order was already processed, it skips the charge entirely and just
 * acks -- so a crash between "charge succeeded" and "ack" no longer causes
 * a duplicate charge, even though the message is still redelivered.
 */
public class IdempotentConsumer {
    private final MessageQueue queue;
    private final PaymentProcessor paymentProcessor;
    private final Set<String> crashedOnce = new HashSet<>();
    private final Set<String> alreadyCharged = new HashSet<>();

    public IdempotentConsumer(MessageQueue queue, PaymentProcessor paymentProcessor) {
        this.queue = queue;
        this.paymentProcessor = paymentProcessor;
    }

    public void processAll() {
        String message;
        while ((message = queue.peek()) != null) {
            String[] parts = message.split(":");
            String orderId = parts[0];
            double amount = Double.parseDouble(parts[1]);

            if (alreadyCharged.contains(orderId)) {
                System.out.println("Redelivery of " + orderId + " detected -- already charged, skipping");
            } else {
                paymentProcessor.charge(orderId, amount);
                alreadyCharged.add(orderId);
            }

            if (orderId.equals("order-2") && crashedOnce.add(orderId)) {
                System.out.println("Simulated crash: consumer died after charging " + orderId
                        + " but before acking it");
                continue; // message stays in the queue, unacked, will be redelivered
            }

            queue.ack(message);
        }
    }
}
