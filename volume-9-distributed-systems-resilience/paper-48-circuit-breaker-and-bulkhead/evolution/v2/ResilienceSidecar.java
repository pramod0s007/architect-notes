class ResilienceSidecar {
    private final int maxRetries;
    private final long backoffMs;

    ResilienceSidecar(int maxRetries, long backoffMs) {
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
    }

    <T> T callWithRetry(Callable<T> remoteCall, T failureValue) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                return remoteCall.call();
            } catch (TimeoutException e) {
                attempts++;
                sleep(backoffMs * attempts); // increasing backoff, same for every caller
            }
        }
        return failureValue;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
