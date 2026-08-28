public class Main {
    public static void main(String[] args) {
        OrderSaga saga = new OrderSaga();
        Order order = new Order("ORD-NO-CAPACITY");

        String result = saga.placeOrder(order);

        System.out.println("v3: same failure, compensating transactions run in reverse order.");
        System.out.println("    result=" + result);
        System.out.println("    payment charged=" + saga.payments().isCharged(order) + " (refunded)");
        System.out.println("    inventory reserved=" + saga.inventoryService().isReserved(order) + " (released)");
        System.out.println("    Every prior step is undone -- no leaked state.");
    }
}
