// Card payments have no concept of an exchange-rate lookup or a settlement webhook —
// this class is forced to implement methods it cannot honor.
class StripeGatewayBloated implements PaymentGatewayBloated {
    public SettlementStatus charge(double amount) {
        return SettlementStatus.CONFIRMED;
    }

    public SettlementStatus chargeRecurring(double amount, String scheduleId) {
        return SettlementStatus.CONFIRMED;
    }

    public void registerWebhookForSettlementUpdates(String url) {
        throw new UnsupportedOperationException("Stripe settles synchronously — no webhook needed");
    }

    public double getExchangeRate(String fromCurrency, String toCurrency) {
        throw new UnsupportedOperationException("Stripe charges are single-currency — no exchange rate");
    }
}
