// Business rules creeping into the routing layer: a loyalty discount and a
// fraud check that belong in a service, not in the gateway.
class OverloadedApiGateway {
    String placeOrder(Order order) {
        double discount = order.getCustomerId().startsWith("VIP-") ? 0.10 : 0.0;
        double discountedTotal = order.getTotal() * (1 - discount);

        if (discountedTotal > 5000 && !order.getCustomerId().startsWith("VERIFIED-")) {
            return "REJECTED: requires manual fraud review";
        }

        ChargeLog.record(order.getId(), discountedTotal); // only the gateway knows this rule ran
        return "CONFIRMED: " + order.getId() + " charged=" + discountedTotal;
    }
}
