public final class IdleState implements WatchState {

    @Override
    public void start(StopWatch watch) {
        watch.beginSegment();
        watch.transitionTo(new RunningState());
    }

    @Override
    public void pause(StopWatch watch) {
        throw new IllegalStateException("Cannot pause while idle");
    }

    @Override
    public void resume(StopWatch watch) {
        throw new IllegalStateException("Cannot resume while idle");
    }

    @Override
    public void stop(StopWatch watch) {
        // Already stopped
    }

    @Override
    public String name() {
        return "IDLE";
    }
}
