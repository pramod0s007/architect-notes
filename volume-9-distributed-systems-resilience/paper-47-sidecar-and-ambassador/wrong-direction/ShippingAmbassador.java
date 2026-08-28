// Separately hardcodes knowledge of the Shipping backend's specific error type and
// retry count -- a DIFFERENT bespoke policy, even though the underlying need is identical.
class ShippingAmbassador {
    private int failuresRemaining = 2; // SAME flakiness as Inventory

    String callShipping(Order order) {
        try {
            return callShippingBackend() ? "TRK-" + order.getId() : "SCHEDULING_FAILED";
        } catch (ShippingBackendError e) {
            return retryShippingSpecificWay(order);
        }
    }

    private String retryShippingSpecificWay(Order order) {
        try {
            return callShippingBackend() ? "TRK-" + order.getId() : "SCHEDULING_FAILED";
        } catch (ShippingBackendError e) {
            return "SCHEDULING_FAILED"; // only 1 retry, Shipping's own (different) count
        }
    }

    private boolean callShippingBackend() throws ShippingBackendError {
        if (failuresRemaining > 0) {
            failuresRemaining--;
            throw new ShippingBackendError();
        }
        return true;
    }
}
