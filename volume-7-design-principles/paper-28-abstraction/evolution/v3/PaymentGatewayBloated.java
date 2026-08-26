import java.util.List;

// Too much abstraction: this interface promises capabilities only ONE implementation
// (crypto) actually has, forcing every other implementation to stub or throw.
interface PaymentGatewayBloated {
    SettlementStatus charge(double amount);
    SettlementStatus chargeRecurring(double amount, String scheduleId);
    void registerWebhookForSettlementUpdates(String url); // only crypto needs this
    double getExchangeRate(String fromCurrency, String toCurrency); // only crypto needs this
}
