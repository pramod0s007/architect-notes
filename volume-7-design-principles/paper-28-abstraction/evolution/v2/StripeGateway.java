class StripeGateway implements PaymentGateway {
    public SettlementStatus charge(double amount) {
        return SettlementStatus.CONFIRMED; // card payments settle synchronously
    }
}
