class ShippingService {
    String schedule(Order order) {
        String tracking = "TRK-" + order.getId();
        SharedOrderDatabase.execute("shipping_status", order.getId(), "SCHEDULED"); // the SAME database, again
        return tracking;
    }
}
