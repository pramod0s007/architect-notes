class ShippingService {
    String schedule(Order order) {
        String tracking = "TRK-" + order.getId();
        System.out.println("[shipping] scheduled " + order.getId() + " tracking=" + tracking);
        return tracking;
    }
}
