public class Main {
    public static void main(String[] args) {
        InventoryAmbassador inventoryAmbassador = new InventoryAmbassador();
        ShippingAmbassador shippingAmbassador = new ShippingAmbassador();
        Order order = new Order("ORD-4");

        System.out.println("Inventory ambassador (its own 2-retry policy): " + inventoryAmbassador.callInventory(order));
        System.out.println("Shipping ambassador (its own 1-retry policy):  " + shippingAmbassador.callShipping(order));
        System.out.println("v-wrong: two bespoke ambassadors, two different retry counts again --");
        System.out.println("         the duplication moved from the services into the sidecars.");
    }
}
