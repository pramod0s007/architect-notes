// v3: SRP applied — each class owned by one team, one reason to change
public class OrderValidator {
    public void validate(Order order) {
        if (order.getItems().isEmpty()) throw new IllegalArgumentException("No items");
        if (order.getCustomer() == null) throw new IllegalArgumentException("No customer");
        System.out.println("Order validated: " + order.getId());
    }
}
