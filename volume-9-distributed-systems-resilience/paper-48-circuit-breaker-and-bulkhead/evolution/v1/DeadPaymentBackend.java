class DeadPaymentBackend {
    boolean charge(Order order) throws TimeoutException {
        try {
            Thread.sleep(80); // the hung connection, before the timeout fires
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new TimeoutException("payment backend unreachable");
    }
}
