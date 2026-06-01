import java.time.Instant;

/**
 * Immutable event published whenever a stock price changes.
 */
public final class StockPriceEvent {

    private final String symbol;
    private final double previousPrice;
    private final double currentPrice;
    private final Instant timestamp;

    public StockPriceEvent(String symbol, double previousPrice, double currentPrice) {
        this.symbol        = symbol;
        this.previousPrice = previousPrice;
        this.currentPrice  = currentPrice;
        this.timestamp     = Instant.now();
    }

    public String getSymbol()         { return symbol; }
    public double getPreviousPrice()  { return previousPrice; }
    public double getCurrentPrice()   { return currentPrice; }
    public Instant getTimestamp()     { return timestamp; }

    /** Returns the percentage change (positive = up, negative = down). */
    public double getPercentChange() {
        if (previousPrice == 0) return 0;
        return ((currentPrice - previousPrice) / previousPrice) * 100.0;
    }

    @Override
    public String toString() {
        return String.format("%s: $%.2f → $%.2f (%+.2f%%)",
                symbol, previousPrice, currentPrice, getPercentChange());
    }
}
