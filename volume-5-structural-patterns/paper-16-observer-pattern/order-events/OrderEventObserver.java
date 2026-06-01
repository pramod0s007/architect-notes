package observer.orderevents;

public interface OrderEventObserver {
    void onOrderPlaced(OrderPlacedEvent event);
}
