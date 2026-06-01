/**
 * Observer interface — implement to receive stock price change notifications.
 */
public interface StockPriceObserver {

    void onPriceChanged(StockPriceEvent event);
}
