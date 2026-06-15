public class OrderAuditLogger {
    public void log(Order order, OrderEvent event) {
        // Legal/Compliance team owns this class exclusively
        System.out.println("AUDIT [COMPLIANCE]: order=" + order.getId() + " event=" + event);
    }
}
