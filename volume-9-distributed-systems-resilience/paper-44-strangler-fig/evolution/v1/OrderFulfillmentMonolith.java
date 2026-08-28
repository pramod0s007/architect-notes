class OrderFulfillmentMonolith {
    private final Ledger ledger = new Ledger();
    private final Warehouse warehouse = new Warehouse();

    String placeOrder(Order order) {
        if (!order.isValid()) return "REJECTED: invalid order";

        boolean charged = ledger.charge(order.getCustomerId(), order.getTotal());
        if (!charged) return "REJECTED: payment failed";

        boolean reserved = warehouse.reserve(order.getItems());
        if (!reserved) {
            ledger.refund(order.getCustomerId(), order.getTotal());
            return "REJECTED: out of stock";
        }

        return "CONFIRMED: " + order.getId();
    }
}
