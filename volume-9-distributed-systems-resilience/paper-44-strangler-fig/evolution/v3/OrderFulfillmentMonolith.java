class OrderFulfillmentMonolith {
    private final PaymentRouter paymentRouter = new PaymentRouter();
    private final Warehouse warehouse = new Warehouse();

    String placeOrder(Order order) {
        if (!order.isValid()) return "REJECTED: invalid order";

        boolean charged = paymentRouter.charge(order);
        if (!charged) return "REJECTED: payment failed";

        boolean reserved = warehouse.reserve(order.getItems());
        if (!reserved) return "REJECTED: out of stock";

        return "CONFIRMED: " + order.getId();
    }
}
