class InventoryService {
    boolean reserve(Order order) {
        System.out.println("[inventory] reserved items for " + order.getId());
        return true;
    }
}
