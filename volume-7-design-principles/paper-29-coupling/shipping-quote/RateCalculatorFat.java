// Only reads 2 of 5 fields, but a change to any of the other 3 still risks breaking this class
// during a refactor, and every caller must build a full ShippingQuoteFat to call it.
class RateCalculatorFat {
    double quote(ShippingQuoteFat request) {
        double base = request.weightKg * 2.5;
        return request.destinationZip.startsWith("0") ? base * 1.2 : base;
    }
}
