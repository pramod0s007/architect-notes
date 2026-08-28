public class Main {
    public static void main(String[] args) {
        CircuitBreaker sharedBreaker = new CircuitBreaker(2, 5000);
        PaymentRouterWithBreaker paymentRouter = new PaymentRouterWithBreaker(sharedBreaker);
        InventoryServiceWithSharedBreaker inventory = new InventoryServiceWithSharedBreaker(sharedBreaker);

        // Payment is dead. Two calls are enough to trip the shared breaker.
        for (int i = 1; i <= 2; i++) {
            boolean charged = paymentRouter.charge(new Order("PAY-" + i));
            System.out.println("Payment attempt " + i + ": charged=" + charged);
        }

        // Inventory's own backend is completely healthy -- never called yet.
        int rejectedByBreaker = 0;
        for (int i = 1; i <= 5; i++) {
            boolean reserved = inventory.reserve(new Order("INV-" + i));
            if (!reserved) rejectedByBreaker++;
        }

        System.out.println("Inventory calls rejected by the (shared) breaker: " + rejectedByBreaker + "/5");
        System.out.println("Inventory's real, healthy backend was actually invoked: " + inventory.backendInvocationCount() + "/5 times");
        System.out.println("The backend is fine. The breaker that blocked it tripped because of PAYMENT's failures.");
    }
}
