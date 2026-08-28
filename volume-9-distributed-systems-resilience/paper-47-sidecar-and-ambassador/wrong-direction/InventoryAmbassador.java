// Hardcodes knowledge of the Inventory backend's specific error type and retry count --
// its OWN bespoke policy, not a shared one.
class InventoryAmbassador {
    private int failuresRemaining = 2;

    boolean callInventory(Order order) {
        try {
            return callInventoryBackend();
        } catch (InventoryBackendError e) {
            return retryInventorySpecificWay();
        }
    }

    private boolean retryInventorySpecificWay() {
        try {
            return callInventoryBackend();
        } catch (InventoryBackendError e) {
            try {
                return callInventoryBackend(); // 2 retries, Inventory's own count
            } catch (InventoryBackendError e2) {
                return false;
            }
        }
    }

    private boolean callInventoryBackend() throws InventoryBackendError {
        if (failuresRemaining > 0) {
            failuresRemaining--;
            throw new InventoryBackendError();
        }
        return true;
    }
}
