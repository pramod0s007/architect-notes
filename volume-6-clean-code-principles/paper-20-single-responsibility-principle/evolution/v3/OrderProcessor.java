// Orchestrator — one reason to change: the flow itself
public class OrderProcessor {
    private final OrderValidator validator;
    private final PaymentProcessor paymentProcessor;
    private final OrderConfirmationService confirmationService;
    private final InventoryUpdater inventoryUpdater;
    private final OrderAuditLogger auditLogger;

    public OrderProcessor(OrderValidator validator, PaymentProcessor paymentProcessor,
                          OrderConfirmationService confirmationService,
                          InventoryUpdater inventoryUpdater, OrderAuditLogger auditLogger) {
        this.validator = validator;
        this.paymentProcessor = paymentProcessor;
        this.confirmationService = confirmationService;
        this.inventoryUpdater = inventoryUpdater;
        this.auditLogger = auditLogger;
    }

    public void processOrder(Order order) {
        validator.validate(order);
        paymentProcessor.charge(order);
        confirmationService.sendConfirmation(order);
        inventoryUpdater.updateInventory(order);
        auditLogger.log(order, OrderEvent.PROCESSED);
    }
}
