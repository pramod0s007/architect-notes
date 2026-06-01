package observer.orderevents;

public class WarehouseObserver implements OrderEventObserver {

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[Warehouse] Packing request raised for order: "
            + event.getOrder().getId()
            + " | Items: " + event.getOrder().getItemCount());
    }
}
