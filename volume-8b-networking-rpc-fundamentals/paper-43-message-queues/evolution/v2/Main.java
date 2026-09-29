/**
 * The queue decouples the producer from the consumer -- the producer no
 * longer blocks or fails when the consumer is slow or briefly down, unlike
 * evolution/v1's direct synchronous call. But at-most-once delivery
 * (dequeue removes immediately, before processing finishes) means a crash
 * mid-processing still loses the message permanently.
 */
public class Main {
    public static void main(String[] args) {
        MessageQueue queue = new MessageQueue();
        queue.enqueue("order-1:49.99");
        queue.enqueue("order-2:79.99");
        queue.enqueue("order-3:19.99");

        System.out.println("Queue depth before processing: " + queue.size());

        Consumer consumer = new Consumer(queue, new PaymentProcessor());
        consumer.processAll();

        System.out.println();
        System.out.println("Queue depth after processing: " + queue.size());
        System.out.println();
        System.out.println("CONFIRMED FAILURE: order-2 was dequeued, then crashed during"
                + " processing, and is now gone from both the queue AND the payment processor --"
                + " permanently lost, exactly like evolution/v1, just one layer removed. The"
                + " queue decoupled the producer from the consumer's *availability*, but not from"
                + " the consumer's *failures during processing*.");
    }
}
