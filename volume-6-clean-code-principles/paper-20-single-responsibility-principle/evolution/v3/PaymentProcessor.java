public class PaymentProcessor {
    public void charge(Order order) {
        // Payments team owns this class exclusively
        System.out.println("Charging " + order.getTotal() + " via region=" + order.getCustomer().getRegion());
    }
}
