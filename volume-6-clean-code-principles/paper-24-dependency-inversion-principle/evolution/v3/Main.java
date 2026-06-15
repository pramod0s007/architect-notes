public class Main {

    public static void main(String[] args) {
        System.out.println("=== Production wiring ===");
        OrderService production = new OrderService(
                new MySQLOrderRepository(),
                new SesEmailService(),
                new StripeFraudDetection()
        );
        Order realOrder = new Order("ORD-101", 299.99, "carol@example.com", true);
        production.placeOrder(realOrder);

        System.out.println();

        System.out.println("=== Test wiring (no infrastructure required) ===");
        InMemoryOrderRepository testRepo = new InMemoryOrderRepository();
        OrderService testService = new OrderService(
                testRepo,
                new NoOpEmailService(),
                order -> false   // lambda: no order is fraudulent in tests
        );
        Order testOrder = new Order("ORD-T01", 49.99, "test@example.com", false);
        testService.placeOrder(testOrder);

        System.out.println("Stored orders in test repo: " + testRepo.getSaved().size());
    }
}
