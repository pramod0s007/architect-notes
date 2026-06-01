public final class RunningState implements WatchState {

    @Override
    public void start(StopWatch watch) {
        throw new IllegalStateException("Already running");
    }

    @Override
    public void pause(StopWatch watch) {
        watch.endSegment();
        watch.transitionTo(new PausedState());
    }

    @Override
    public void resume(StopWatch watch) {
        throw new IllegalStateException("Cannot resume while running");
    }

    @Override
    public void stop(StopWatch watch) {
        watch.endSegment();
        watch.transitionTo(new IdleState());
    }

    @Override
    public String name() {
        return "RUNNING";
    }
}
