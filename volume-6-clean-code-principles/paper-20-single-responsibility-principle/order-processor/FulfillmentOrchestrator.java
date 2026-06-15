// SRP orchestrator: its only reason to change is the fulfillment sequence itself.
// Each collaborator is owned by a separate team and changes independently.
public class FulfillmentOrchestrator {
    private final StockChecker       stockChecker;
    private final ShipmentDispatcher dispatcher;
    private final FulfillmentNotifier notifier;
    private final FulfillmentRecorder recorder;

    public FulfillmentOrchestrator(StockChecker stockChecker,
                                   ShipmentDispatcher dispatcher,
                                   FulfillmentNotifier notifier,
                                   FulfillmentRecorder recorder) {
        this.stockChecker = stockChecker;
        this.dispatcher   = dispatcher;
        this.notifier     = notifier;
        this.recorder     = recorder;
    }

    public String fulfill(ShipmentOrder order) {
        stockChecker.reserve(order);
        String trackingId = dispatcher.dispatch(order);
        notifier.notify(order, trackingId);
        recorder.record(order);
        return trackingId;
    }
}
