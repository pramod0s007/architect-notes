public class Main {
    public static void main(String[] args) {
        PaymentGateway wire = new WireTransferGateway();
        boolean charged = wire.charge(50000.0);
        System.out.println("WireTransferGateway.charge() returned: " + charged);

        RefundService refunds = new RefundService();
        if (charged) {
            // A refund gets processed 10 minutes later against a payment that
            // will not actually clear for another 3 days.
            refunds.refund(wire, 50000.0);
        }
        System.out.println("Bug: refund processed against unsettled wire transfer.");
    }
}
