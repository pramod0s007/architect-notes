public class Order {
    public final String id;
    public final double total;
    public final int loyaltyYears;
    public final boolean hasPromoCode;
    public final String customerId;

    public Order(String id, double total, int loyaltyYears, boolean hasPromoCode, String customerId) {
        this.id = id;
        this.total = total;
        this.loyaltyYears = loyaltyYears;
        this.hasPromoCode = hasPromoCode;
        this.customerId = customerId;
    }
}
