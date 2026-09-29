/**
 * Scenario 1: at-least-once delivery via peek()+ack() fixes evolution/v2's
 * message loss (order-2 is redelivered instead of disappearing) -- but
 * without any memory of what's already been processed, the redelivery
 * causes order-2 to be charged twice.
 *
 * Scenario 2: the identical crash-and-redeliver sequence, replayed against
 * a consumer that tracks already-processed order IDs. The message is still
 * redelivered exactly the same way, but the duplicate charge is skipped.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Scenario 1: at-least-once delivery without idempotency ===");
        MessageQueue queue1 = new MessageQueue();
        queue1.enqueue("order-1:49.99");
        queue1.enqueue("order-2:79.99");
        queue1.enqueue("order-3:19.99");

        PaymentProcessor paymentProcessor1 = new PaymentProcessor();
        new Consumer(queue1, paymentProcessor1).processAll();

        System.out.println();
        System.out.println("order-2 was charged " + paymentProcessor1.getChargeCount("order-2")
                + " time(s). Queue depth after processing: " + queue1.size());
        System.out.println();
        System.out.println("CONFIRMED FAILURE: the message was NOT lost this time -- redelivery"
                + " worked exactly as intended. But the consumer has no idea it already charged"
                + " order-2 before it crashed, so the redelivered message triggers a second,"
                + " duplicate charge.");

        System.out.println();
        System.out.println("=== Scenario 2: at-least-once delivery WITH idempotency ===");
        MessageQueue queue2 = new MessageQueue();
        queue2.enqueue("order-1:49.99");
        queue2.enqueue("order-2:79.99");
        queue2.enqueue("order-3:19.99");

        PaymentProcessor paymentProcessor2 = new PaymentProcessor();
        new IdempotentConsumer(queue2, paymentProcessor2).processAll();

        System.out.println();
        System.out.println("order-2 was charged " + paymentProcessor2.getChargeCount("order-2")
                + " time(s). Queue depth after processing: " + queue2.size());
        System.out.println();
        System.out.println("FIXED: order-2 is still redelivered after the simulated crash --"
                + " the queue's at-least-once guarantee didn't change. But the consumer recognizes"
                + " the redelivery as a duplicate of work it already did, and skips the charge"
                + " while still acking the message so it doesn't loop forever.");
    }
}
