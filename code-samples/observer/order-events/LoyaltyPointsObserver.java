package observer.orderevents;

public class LoyaltyPointsObserver implements OrderEventObserver {

    private static final double POINTS_PER_DOLLAR = 1.5;

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        double points = event.getOrder().getTotalValue() * POINTS_PER_DOLLAR;
        System.out.printf("[Loyalty] %.0f points awarded to customer %s%n",
            points, event.getOrder().getCustomerId());
    }
}
