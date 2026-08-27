// volatile fixes visibility. inFlight++ is still read-modify-write — three steps, not one.
// This still races exactly like the plain int version.
class RequestCounterVolatile {
    private volatile int inFlight = 0;

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
