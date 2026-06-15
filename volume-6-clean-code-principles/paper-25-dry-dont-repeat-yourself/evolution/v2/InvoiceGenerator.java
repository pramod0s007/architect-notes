public class InvoiceGenerator {

    // Copied from CartService. Exact duplicate.
    public double calculateInvoiceTotal(Order order) {
        double discount = 0;
        if (order.loyaltyYears >= 1) {
            discount = order.total * 0.01 * order.loyaltyYears;
            double maxDiscount = order.total * 0.30;
            if (discount > maxDiscount) discount = maxDiscount;
        }
        return order.total - discount;
    }

    public String generate(Order order) {
        double total = calculateInvoiceTotal(order);
        return "Invoice #" + order.id + " | Customer: " + order.customerId + " | Total: " + total;
    }
}
