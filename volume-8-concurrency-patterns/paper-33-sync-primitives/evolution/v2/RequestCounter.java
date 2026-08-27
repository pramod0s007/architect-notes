class RequestCounter {
    private int inFlight = 0;

    void requestStarted() {
        inFlight++;
    }

    void requestFinished() {
        inFlight--;
    }

    int getInFlight() {
        return inFlight;
    }
}
