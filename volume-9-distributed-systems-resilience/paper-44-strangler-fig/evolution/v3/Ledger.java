class Ledger {
    boolean charge(String customerId, double amount) {
        return LegacyPaymentGateway.charge(customerId, amount);
    }

    void refund(String customerId, double amount) {
        LegacyPaymentGateway.refund(customerId, amount);
    }
}
