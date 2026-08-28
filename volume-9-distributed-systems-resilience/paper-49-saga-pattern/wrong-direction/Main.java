public class Main {
    public static void main(String[] args) {
        OrderSagaPartialCompensation saga = new OrderSagaPartialCompensation();
        Order order = new Order("ORD-NO-CAPACITY");

        String result = saga.placeOrder(order);

        System.out.println("wrong-direction: compensation exists, but only per-branch, not exhaustive.");
        System.out.println("    result=" + result);
        System.out.println("    inventory reserved=" + saga.inventoryService().isReserved(order) + " (looks fixed)");
        System.out.println("    payment charged=" + saga.payments().isCharged(order) + " (still charged -- forgotten!)");
        System.out.println("    The customer paid for an order that will never ship, and nothing will ever refund it.");
    }
}
