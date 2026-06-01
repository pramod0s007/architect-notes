package observer.orderevents;

public class Main {

    public static void main(String[] args) {
        OrderService orderService = new OrderService();

        // Register observers — adding new observer = one line, no change to OrderService
        orderService.registerObserver(new EmailNotificationObserver());
        orderService.registerObserver(new WarehouseObserver());
        orderService.registerObserver(new LoyaltyPointsObserver());

        // Place an order — all observers notified automatically
        Order order = new Order("ORD-001", "CUST-42", "alice@example.com", 3, 149.99);
        orderService.placeOrder(order);

        System.out.println("\n--- Adding analytics observer without changing OrderService ---");
        orderService.registerObserver(event ->
            System.out.println("[Analytics] Order tracked: " + event.getOrder().getId()));

        Order order2 = new Order("ORD-002", "CUST-17", "bob@example.com", 1, 29.99);
        orderService.placeOrder(order2);
    }
}
