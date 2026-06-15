public class Main {
    public static void main(String[] args) {
        ShipmentOrder order = new ShipmentOrder(
                "ORD-9821", "cust-004", "alice@example.com",
                "42 Pine Street, Austin TX 78701", "LAPTOP-PRO-15", 2);

        System.out.println("══════════════════════════════════════");
        System.out.println(" Order Fulfillment — SRP Demo");
        System.out.println("══════════════════════════════════════\n");

        System.out.println("--- VIOLATION: OrderFulfillmentProcessor (4 concerns in 1 class) ---");
        OrderFulfillmentProcessor violation = new OrderFulfillmentProcessor();
        String trackingViolation = violation.fulfill(order);
        System.out.println("Tracking: " + trackingViolation);

        System.out.println("\n--- FIXED: FulfillmentOrchestrator (each class = 1 actor) ---");
        FulfillmentOrchestrator orchestrator = new FulfillmentOrchestrator(
                new StockChecker(),
                new ShipmentDispatcher(),
                new FulfillmentNotifier(),
                new FulfillmentRecorder()
        );
        String trackingFixed = orchestrator.fulfill(order);
        System.out.println("Tracking: " + trackingFixed);
    }
}
