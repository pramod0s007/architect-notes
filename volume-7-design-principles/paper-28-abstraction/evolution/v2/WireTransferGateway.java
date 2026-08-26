class WireTransferGateway implements PaymentGateway {
    public SettlementStatus charge(double amount) {
        return SettlementStatus.PENDING; // truthfully — settlement takes 1-3 business days
    }
}
