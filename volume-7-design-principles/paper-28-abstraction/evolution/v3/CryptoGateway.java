// Implements only PaymentGateway — the type system now truthfully tells callers
// that crypto payments cannot be scheduled recurringly, instead of throwing at runtime.
class CryptoGateway implements PaymentGateway {
    public SettlementStatus charge(double amount) {
        return SettlementStatus.PENDING; // confirmations take block-time to finalize
    }
}
