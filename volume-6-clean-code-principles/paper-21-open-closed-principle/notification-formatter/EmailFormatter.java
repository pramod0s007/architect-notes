// EMAIL channel — rich HTML with subject, greeting, item list, and total.
// Marketing team owns this. Adding this class required zero changes to dispatcher.

public class EmailFormatter implements NotificationFormatter {

    @Override
    public String format(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Subject: Your SecureShop Order #").append(order.getId())
          .append(" is Confirmed!\n\n");
        sb.append("Hi ").append(order.getCustomerName()).append(",\n\n");
        sb.append("Great news — your order has been confirmed.\n\n");
        sb.append("Order Summary:\n");
        sb.append("──────────────────────────────\n");
        for (String item : order.getItems()) {
            sb.append("  • ").append(item).append("\n");
        }
        sb.append("──────────────────────────────\n");
        sb.append(String.format("  Total: $%.2f%n%n", order.getTotal()));
        sb.append("Estimated delivery: 3–5 business days.\n");
        sb.append("Track your order at: https://secureshop.com/orders/")
          .append(order.getId()).append("\n\n");
        sb.append("Thanks for shopping with SecureShop!\n");
        sb.append("— The SecureShop Team\n");
        return sb.toString();
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }
}
