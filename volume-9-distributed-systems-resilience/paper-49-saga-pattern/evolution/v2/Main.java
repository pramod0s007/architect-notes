public class Main {
    public static void main(String[] args) {
        OrderSagaNaive saga = new OrderSagaNaive();
        Order order = new Order("ORD-NO-CAPACITY");

        String result = saga.placeOrder(order);

        System.out.println("v2: three services, sequential calls, no compensation.");
        System.out.println("    result=" + result);
        System.out.println("    payment charged=" + saga.payments().isCharged(order));
        System.out.println("    inventory reserved=" + saga.inventoryService().isReserved(order));
        System.out.println("    The customer was charged and inventory is held for an order that will never ship.");
    }
}
