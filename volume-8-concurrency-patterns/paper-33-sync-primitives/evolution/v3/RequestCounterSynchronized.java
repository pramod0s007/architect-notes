class RequestCounterSynchronized {
    private int inFlight = 0;

    synchronized void requestStarted() {
        inFlight++;
    }

    synchronized void requestFinished() {
        inFlight--;
    }

    synchronized int getInFlight() {
        return inFlight;
    }
}
