// Same seam introduced in Paper 44 — reused here as the payment endpoint.
class PaymentRouter {
    private final Ledger legacy = new Ledger();
    private final NewPaymentService modern = new NewPaymentService();

    boolean charge(Order order) {
        if (order.getCustomerId().startsWith("INTL-")) {
            return modern.charge(order.getCustomerId(), order.getTotal());
        }
        return legacy.charge(order.getCustomerId(), order.getTotal());
    }
}
