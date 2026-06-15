public class InvoiceGenerator {

    private final DiscountCalculator discountCalculator;

    public InvoiceGenerator(DiscountCalculator discountCalculator) {
        this.discountCalculator = discountCalculator;
    }

    public String generate(Order order) {
        double total = discountCalculator.calculate(order.total, order.loyaltyYears, order.hasPromoCode);
        return "Invoice #" + order.id + " | Customer: " + order.customerId + " | Total: " + total;
    }
}
