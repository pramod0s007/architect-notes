enum StopWatchState {
    IDLE,
    RUNNING,
    SUSPENDED
}

class StopWatch {

    private StopWatchState state = StopWatchState.IDLE;
    private long startTimeMillis;
    private long elapsedMillis;

    void click() {
        if (state == StopWatchState.IDLE) {
            state = StopWatchState.RUNNING;
            startTimeMillis = System.currentTimeMillis();
            return;
        }

        if (state == StopWatchState.RUNNING) {
            elapsedMillis += System.currentTimeMillis() - startTimeMillis;
            state = StopWatchState.SUSPENDED;
            return;
        }

        if (state == StopWatchState.SUSPENDED) {
            startTimeMillis = System.currentTimeMillis();
            state = StopWatchState.RUNNING;
        }
    }

    long getElapsedTime() {
        if (state == StopWatchState.IDLE) {
            return elapsedMillis;
        }

        if (state == StopWatchState.RUNNING) {
            return elapsedMillis + (System.currentTimeMillis() - startTimeMillis);
        }

        if (state == StopWatchState.SUSPENDED) {
            return elapsedMillis;
        }

        throw new IllegalStateException("Unknown state: " + state);
    }

    void reset() {
        if (state == StopWatchState.IDLE) {
            elapsedMillis = 0;
            return;
        }

        if (state == StopWatchState.RUNNING) {
            state = StopWatchState.IDLE;
            elapsedMillis = 0;
            startTimeMillis = 0;
            return;
        }

        if (state == StopWatchState.SUSPENDED) {
            state = StopWatchState.IDLE;
            elapsedMillis = 0;
        }
    }
}
