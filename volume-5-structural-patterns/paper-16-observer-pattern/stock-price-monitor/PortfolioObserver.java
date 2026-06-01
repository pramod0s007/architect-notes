import java.util.HashMap;
import java.util.Map;

/**
 * Tracks portfolio value by maintaining share holdings per symbol.
 * Recalculates and prints the total value whenever a held stock changes price.
 */
public final class PortfolioObserver implements StockPriceObserver {

    private final String ownerName;
    private final Map<String, Integer> holdings;          // symbol → shares held
    private final Map<String, Double>  currentPrices = new HashMap<>();

    public PortfolioObserver(String ownerName, Map<String, Integer> holdings) {
        this.ownerName = ownerName;
        this.holdings  = Map.copyOf(holdings);
    }

    @Override
    public void onPriceChanged(StockPriceEvent event) {
        String symbol = event.getSymbol();
        if (!holdings.containsKey(symbol)) {
            return;   // this portfolio doesn't hold that stock
        }

        currentPrices.put(symbol, event.getCurrentPrice());

        double total = holdings.entrySet().stream()
                .mapToDouble(e -> e.getValue() * currentPrices.getOrDefault(e.getKey(), 0.0))
                .sum();

        int shares = holdings.get(symbol);
        double gain = shares * (event.getCurrentPrice() - event.getPreviousPrice());

        System.out.printf("  [Portfolio] %s — %s %+.2f (%d shares) | portfolio total: $%,.2f%n",
                ownerName, symbol, gain, shares, total);
    }
}
