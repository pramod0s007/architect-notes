public class Main {
    public static void main(String[] args) {
        InventoryService.simulateLoad = true; // 3s per reserve() call, representing a slow step under load
        ApiGateway gateway = new ApiGateway();

        long start = System.nanoTime();
        String result = gateway.placeOrder(new Order("ORD-1"));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println(result + " (" + elapsedMs + "ms)");
        System.out.println("v2: one slow step in the chain adds its full delay to every single");
        System.out.println("    request's response time -- exactly like a slow method call inside");
        System.out.println("    the old monolith would have.");
    }
}
