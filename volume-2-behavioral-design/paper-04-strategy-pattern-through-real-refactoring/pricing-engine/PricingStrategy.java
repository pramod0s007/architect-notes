/**
 * Strategy interface — isolates pricing-algorithm variation by customer type.
 * Adding a new customer tier never touches the calling code.
 */
public interface PricingStrategy {

    /**
     * Calculate the final price for a line item.
     *
     * @param basePrice  unit price before any discount
     * @param quantity   number of units
     * @return           total price after applying this strategy's discount
     */
    double calculatePrice(double basePrice, int quantity);

    /** Human-readable tier name for receipts and logs. */
    String tierName();

    /** Discount percentage applied by this strategy (0–100). */
    double discountPercent();
}
