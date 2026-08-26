public class Main {
    public static void main(String[] args) {
        PaymentGateway wire = new WireTransferGateway();
        SettlementStatus status = wire.charge(50000.0);
        System.out.println("WireTransferGateway.charge() returned: " + status);

        RefundService refunds = new RefundService();
        refunds.refund(wire, 50000.0); // now correctly blocked

        PaymentGateway stripe = new StripeGateway();
        System.out.println("StripeGateway.charge() returned: " + stripe.charge(50.0));
        refunds.refund(stripe, 50.0); // proceeds correctly
    }
}
