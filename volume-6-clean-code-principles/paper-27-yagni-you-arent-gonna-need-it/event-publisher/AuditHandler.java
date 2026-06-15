/**
 * V2 handler: writes an audit log entry for every placed order.
 *
 * This is the second handler that justified introducing EventBus.
 * Before this requirement existed, building EventBus would have been YAGNI.
 */
public class AuditHandler {

    public void handle(OrderEvent event) {
        System.out.println("[Audit] LOG: order=" + event.getOrderId()
                + " type=" + event.getType()
                + " amount=" + event.getAmount());
    }
}
