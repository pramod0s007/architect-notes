package observer.orderevents;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final List<OrderEventObserver> observers = new ArrayList<>();

    public void registerObserver(OrderEventObserver observer) {
        observers.add(observer);
    }

    public void placeOrder(Order order) {
        // Save order (simulated)
        System.out.println("Order saved: " + order.getId());

        // Publish event to all observers
        OrderPlacedEvent event = new OrderPlacedEvent(order);
        observers.forEach(o -> o.onOrderPlaced(event));
    }
}
