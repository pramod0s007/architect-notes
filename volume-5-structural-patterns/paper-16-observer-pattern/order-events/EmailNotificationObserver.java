package observer.orderevents;

public class EmailNotificationObserver implements OrderEventObserver {

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[Email] Confirmation sent for order: "
            + event.getOrder().getId()
            + " to " + event.getOrder().getCustomerEmail());
    }
}
