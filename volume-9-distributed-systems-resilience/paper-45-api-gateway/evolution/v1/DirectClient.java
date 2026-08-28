class DirectClient {
    private final OrderFulfillmentMonolith orderFulfillmentMonolith;

    DirectClient(OrderFulfillmentMonolith orderFulfillmentMonolith) {
        this.orderFulfillmentMonolith = orderFulfillmentMonolith;
    }

    String placeOrder(Order order) {
        return orderFulfillmentMonolith.placeOrder(order);
    }
}
