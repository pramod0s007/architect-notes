// Two payment paths exist now, but nothing decides between them per-order.
// The only lever is a hardcoded global switch — pick one for EVERY customer.
class OrderFulfillmentMonolith {
    private static final boolean USE_MODERN_FOR_EVERYONE = false; // the entire "routing" logic

    private final Ledger legacy = new Ledger();
    private final NewPaymentService modern = new NewPaymentService();
    private final Warehouse warehouse = new Warehouse();

    String placeOrder(Order order) {
        if (!order.isValid()) return "REJECTED: invalid order";

        boolean charged = USE_MODERN_FOR_EVERYONE
                ? modern.charge(order.getCustomerId(), order.getTotal())
                : legacy.charge(order.getCustomerId(), order.getTotal());
        if (!charged) return "REJECTED: payment failed";

        boolean reserved = warehouse.reserve(order.getItems());
        if (!reserved) return "REJECTED: out of stock";

        return "CONFIRMED: " + order.getId();
    }
}
