/**
 * YAGNI — event-publisher
 *
 * V1: single handler, direct call. Simple, correct, zero over-engineering.
 * V2: EventBus added when the second handler (AuditHandler) was actually needed.
 * The bus was justified by a present requirement — not built speculatively.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== V1: direct handler call (YAGNI respected, one handler) ===");
        OrderPlacedHandler directHandler = new OrderPlacedHandler();
        OrderService v1Service = new OrderService(directHandler);
        v1Service.placeOrder("cust-001", 149.99);

        System.out.println();
        System.out.println("=== V2: EventBus (introduced when AuditHandler arrived) ===");
        EventBus bus = new EventBus();
        InventoryHandler inventory = new InventoryHandler();
        AuditHandler audit         = new AuditHandler();
        bus.register("ORDER_PLACED", inventory::handle);
        bus.register("ORDER_PLACED", audit::handle);

        OrderServiceV2 v2Service = new OrderServiceV2(bus);
        v2Service.placeOrder("cust-002", 299.00);

        System.out.println();
        System.out.println("V1 was the right choice until a second handler arrived.");
        System.out.println("V2 EventBus is 25 lines — no framework needed.");
    }
}
