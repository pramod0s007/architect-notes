import java.util.HashSet;
import java.util.Set;

/**
 * Reads a message with peek() (not removed until explicitly acked) and only
 * calls ack() after processing succeeds. If processing crashes AFTER the
 * real side effect already happened but BEFORE ack() is reached, the
 * message stays in the queue and gets redelivered -- and because this
 * consumer has no memory of which orders it already charged, redelivery
 * means charging the same order again.
 */
public class Consumer {
    private final MessageQueue queue;
    private final PaymentProcessor paymentProcessor;
    private final Set<String> crashedOnce = new HashSet<>();

    public Consumer(MessageQueue queue, PaymentProcessor paymentProcessor) {
        this.queue = queue;
        this.paymentProcessor = paymentProcessor;
    }

    public void processAll() {
        String message;
        while ((message = queue.peek()) != null) {
            String[] parts = message.split(":");
            String orderId = parts[0];
            double amount = Double.parseDouble(parts[1]);

            paymentProcessor.charge(orderId, amount); // the real side effect happens first

            if (orderId.equals("order-2") && crashedOnce.add(orderId)) {
                System.out.println("Simulated crash: consumer died after charging " + orderId
                        + " but before acking it");
                continue; // message stays in the queue, unacked, will be redelivered
            }

            queue.ack(message);
        }
    }
}
