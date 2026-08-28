class InventoryServiceWithOwnDb {
    boolean reserve(Order order) {
        InventoryOwnedDatabase.insert(order.getId(), "RESERVED");
        return true;
    }
}
