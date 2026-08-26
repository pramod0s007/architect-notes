// Fixed: no shared static state. Each caller passes the rate it means, explicitly.
class DiscountService {
    double priceOf(double amount, double discountRate) {
        return amount * (1 - discountRate);
    }
}
