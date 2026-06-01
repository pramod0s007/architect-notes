/**
 * Fires an alert when a stock price moves more than {@code thresholdPercent}
 * in either direction (e.g. sudden crash or spike).
 */
public final class PriceAlertObserver implements StockPriceObserver {

    private final double thresholdPercent;

    public PriceAlertObserver(double thresholdPercent) {
        this.thresholdPercent = thresholdPercent;
    }

    @Override
    public void onPriceChanged(StockPriceEvent event) {
        double change = Math.abs(event.getPercentChange());
        if (change >= thresholdPercent) {
            System.out.printf("  [ALERT]     %s moved %.1f%% — THRESHOLD BREACHED (limit: %.1f%%)%n",
                    event.getSymbol(), event.getPercentChange(), thresholdPercent);
        } else {
            System.out.printf("  [Alert]     %s moved %.1f%% — within normal range%n",
                    event.getSymbol(), event.getPercentChange());
        }
    }
}
