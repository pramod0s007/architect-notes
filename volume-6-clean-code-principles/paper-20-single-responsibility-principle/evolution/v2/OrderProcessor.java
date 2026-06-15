// v2: Pressure builds — 5 teams now own pieces of this class
// Payments team, Marketing team, Legal team, Ops team, Product team
// Each change requires redeploying and retesting everything
public class OrderProcessor {

    void processOrder(Order order) {
        validateOrder(order);       // Product team
        chargePayment(order);       // Payments team
        sendConfirmation(order);    // Marketing team
        updateInventory(order);     // Ops team
        writeAuditLog(order);       // Legal/Compliance team
    }

    private void validateOrder(Order order) {
        if (order.getItems().isEmpty()) throw new IllegalArgumentException("No items");
        if (order.getCustomer() == null) throw new IllegalArgumentException("No customer");
        // Digital vs physical product rules added by product team...
        // Growing to 80 lines
    }

    private void chargePayment(Order order) {
        // Stripe, Braintree, or PayPal based on region — added by payments team
        // 90 lines
        System.out.println("Charging via " + order.getCustomer().getRegion() + " provider");
    }

    private void sendConfirmation(Order order) {
        // Dynamic HTML template, image resizing — added by marketing team
        // 120 lines
        System.out.println("Rich email sent to: " + order.getCustomer().getEmail());
    }

    private void updateInventory(Order order) {
        // Idempotency + retry — added by ops team
        // 60 lines
        System.out.println("Inventory updated with retry");
    }

    private void writeAuditLog(Order order) {
        // Compliance format reviewed by Legal quarterly
        // 70 lines
        System.out.println("Audit log: COMPLIANCE FORMAT " + order.getId());
    }
}
