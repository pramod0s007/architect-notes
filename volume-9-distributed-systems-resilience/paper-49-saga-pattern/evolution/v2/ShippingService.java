class ShippingService {
    // simulates a warehouse capacity failure for one specific order
    String schedule(Order order) {
        if (order.getId().equals("ORD-NO-CAPACITY")) {
            return null;
        }
        return "TRK-" + order.getId();
    }
}
