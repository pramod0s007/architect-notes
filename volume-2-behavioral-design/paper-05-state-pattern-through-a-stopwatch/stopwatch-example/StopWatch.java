/**
 * Delegates behavior to the active {@link WatchState}.
 */
public final class StopWatch {

    private WatchState state = new IdleState();
    private long elapsedMillis;
    private long segmentStartMillis;

    public void start() {
        state.start(this);
    }

    public void pause() {
        state.pause(this);
    }

    public void resume() {
        state.resume(this);
    }

    public void stop() {
        state.stop(this);
    }

    public String stateName() {
        return state.name();
    }

    public long elapsedMillis() {
        if (state instanceof RunningState) {
            return elapsedMillis + (System.currentTimeMillis() - segmentStartMillis);
        }
        return elapsedMillis;
    }

    void transitionTo(WatchState next) {
        this.state = next;
    }

    void beginSegment() {
        segmentStartMillis = System.currentTimeMillis();
    }

    void endSegment() {
        elapsedMillis += System.currentTimeMillis() - segmentStartMillis;
        segmentStartMillis = 0L;
    }
}
