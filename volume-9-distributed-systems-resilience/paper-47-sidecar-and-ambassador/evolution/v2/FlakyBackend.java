class FlakyBackend {
    private int failuresRemaining;

    FlakyBackend(int failuresRemaining) {
        this.failuresRemaining = failuresRemaining;
    }

    boolean call() throws TimeoutException {
        if (failuresRemaining > 0) {
            failuresRemaining--;
            throw new TimeoutException();
        }
        return true;
    }
}
