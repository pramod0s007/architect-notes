public class Main {
    public static void main(String[] args) {
        System.out.println("Data independence check: schema change in InventoryService cannot");
        System.out.println("touch ShippingService's store -- they no longer share a table.");
        System.out.println();

        InventoryService.simulateLoad = true;
        ApiGateway gateway = new ApiGateway();

        long start = System.nanoTime();
        String result = gateway.placeOrder(new Order("ORD-1"));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println(result + " (" + elapsedMs + "ms)");
        System.out.println("v3: data ownership fixed the schema-coupling risk. Response time is");
        System.out.println("    STILL ~3s -- the synchronous call structure is unchanged, and that's");
        System.out.println("    a separate problem (Paper 48).");
    }
}
