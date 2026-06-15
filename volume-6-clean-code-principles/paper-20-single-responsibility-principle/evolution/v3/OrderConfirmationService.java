public class OrderConfirmationService {
    public void sendConfirmation(Order order) {
        // Marketing team owns this class exclusively
        System.out.println("Confirmation email sent to: " + order.getCustomer().getEmail());
    }
}
