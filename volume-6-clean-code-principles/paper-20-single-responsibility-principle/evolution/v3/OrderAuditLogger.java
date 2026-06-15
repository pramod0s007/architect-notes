public class OrderAuditLogger {
    public void log(Order order) {
        // Legal/Compliance team owns this class exclusively
        System.out.println("AUDIT [COMPLIANCE]: order=" + order.getId() + " status=PROCESSED");
    }
}
