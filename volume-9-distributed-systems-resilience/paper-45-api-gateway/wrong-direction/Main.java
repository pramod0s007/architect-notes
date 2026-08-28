import java.util.List;

public class Main {
    public static void main(String[] args) {
        OverloadedApiGateway gateway = new OverloadedApiGateway();

        Order regularOrder = new Order("ORD-6", "CUST-6", 100.0);
        Order vipOrder = new Order("ORD-7", "VIP-1", 100.0);

        System.out.println(gateway.placeOrder(regularOrder));
        System.out.println(gateway.placeOrder(vipOrder));

        System.out.println();
        System.out.println("Batch reconciliation (has no idea the VIP discount rule exists):");
        new BatchReconciliationJob().reconcile(List.of(regularOrder, vipOrder));
        System.out.println("v-wrong: the VIP order's discount is invisible outside the gateway --");
        System.out.println("         reconciliation flags a real charge as a mismatch.");
    }
}
