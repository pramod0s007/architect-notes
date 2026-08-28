class Ledger {
    boolean charge(String customerId, double amount) {
        return LegacyPaymentGateway.charge(customerId, amount);
    }
}
