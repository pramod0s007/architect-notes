class RefundService {
    void refund(PaymentGateway gateway, double amount) {
        SettlementStatus status = gateway.charge(0.0); // simplified: re-check current status
        if (status == SettlementStatus.PENDING) {
            System.out.println("Refund blocked: payment is still PENDING settlement.");
            return;
        }
        System.out.println("Processing refund of " + amount + " against a CONFIRMED payment.");
    }
}
