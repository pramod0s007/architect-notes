// The seam: exactly one place decides which payment path an order takes.
class PaymentRouter {
    private final Ledger legacy = new Ledger();
    private final NewPaymentService modern = new NewPaymentService();

    boolean charge(Order order) {
        if (isInternationalCustomer(order)) {
            return modern.charge(order.getCustomerId(), order.getTotal());
        }
        return legacy.charge(order.getCustomerId(), order.getTotal());
    }

    private boolean isInternationalCustomer(Order order) {
        return order.getCustomerId().startsWith("INTL-");
    }
}
