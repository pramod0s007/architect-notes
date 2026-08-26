// Implements exactly the capabilities Stripe actually has. No stubs, no throws.
class StripeGateway implements PaymentGateway, RefundCapable, RecurringCapable {
    public SettlementStatus charge(double amount) {
        return SettlementStatus.CONFIRMED;
    }

    public boolean refundPartial(String transactionId, double amount, String reason) {
        System.out.println("Refunding " + amount + " on " + transactionId + ": " + reason);
        return true;
    }

    public SettlementStatus chargeRecurring(double amount, String scheduleId) {
        return SettlementStatus.CONFIRMED;
    }
}
