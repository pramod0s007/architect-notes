import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Subject (publisher) that tracks stock prices and notifies all registered
 * observers whenever a price changes.
 *
 * <p>Observers know nothing about each other; the publisher knows nothing
 * about what observers do with the event.
 */
public final class StockPricePublisher {

    private final List<StockPriceObserver> observers   = new ArrayList<>();
    private final Map<String, Double>      lastPrices  = new HashMap<>();

    // ── Observer management ────────────────────────────────────────────────────

    public void registerObserver(StockPriceObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(StockPriceObserver observer) {
        observers.remove(observer);
    }

    // ── Price update ──────────────────────────────────────────────────────────

    /**
     * Updates the price for {@code symbol} and publishes a
     * {@link StockPriceEvent} to all registered observers if the price changed.
     */
    public void updatePrice(String symbol, double newPrice) {
        double previous = lastPrices.getOrDefault(symbol, newPrice);
        lastPrices.put(symbol, newPrice);

        if (Double.compare(previous, newPrice) != 0) {
            StockPriceEvent event = new StockPriceEvent(symbol, previous, newPrice);
            notifyObservers(event);
        }
    }

    private void notifyObservers(StockPriceEvent event) {
        for (StockPriceObserver observer : observers) {
            observer.onPriceChanged(event);
        }
    }
}
