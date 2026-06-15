// v1: One class, one flow — correct for simple requirements
// No SRP issue: same team owns the whole flow
public class OrderProcessor {

    public void processOrder(Order order) {
        // validate
        if (order.getItems().isEmpty()) throw new IllegalArgumentException("No items");
        if (order.getCustomer() == null) throw new IllegalArgumentException("No customer");

        // charge
        System.out.println("Charging: " + order.getTotal());

        // notify
        System.out.println("Email sent to: " + order.getCustomer().getEmail());

        // persist
        System.out.println("Order saved: " + order.getId());
    }
}
