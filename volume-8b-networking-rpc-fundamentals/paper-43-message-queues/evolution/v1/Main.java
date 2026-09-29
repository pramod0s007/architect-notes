/**
 * Places three orders back-to-back, direct and synchronous. The second
 * order's payment call fails (simulated outage) -- and because the caller
 * has no queue to fall back on, that order is simply never charged, with
 * no way for the system to know it needs to be retried later.
 */
public class Main {
    public static void main(String[] args) {
        OrderService orderService = new OrderService(new PaymentProcessor());

        orderService.placeOrder("order-1", 49.99);
        orderService.placeOrder("order-2", 79.99);
        orderService.placeOrder("order-3", 19.99);

        System.out.println();
        System.out.println("CONFIRMED FAILURE: order-2 was never charged and there is no record"
                + " anywhere that it still needs to be. The producer (OrderService) and the"
                + " consumer (PaymentProcessor) are directly coupled -- a failure in one is an"
                + " immediate, unrecoverable failure in the other.");
    }
}
