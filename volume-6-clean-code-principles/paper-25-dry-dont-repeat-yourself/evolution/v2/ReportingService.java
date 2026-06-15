public class ReportingService {

    // Copied from CartService. Cap is missing entirely — discount can exceed 100%.
    public double calculateDiscountedTotal(Order order) {
        double discount = 0;
        if (order.loyaltyYears >= 1) {
            discount = order.total * 0.01 * order.loyaltyYears;
            // Missing cap — copied and edited carelessly
        }
        return order.total - discount;
    }

    public void printReport(Order order) {
        System.out.println("Report | Order: " + order.id + " | Total: " + calculateDiscountedTotal(order));
    }
}
