public final class PausedState implements WatchState {

    @Override
    public void start(StopWatch watch) {
        throw new IllegalStateException("Use resume() from paused");
    }

    @Override
    public void pause(StopWatch watch) {
        throw new IllegalStateException("Already paused");
    }

    @Override
    public void resume(StopWatch watch) {
        watch.beginSegment();
        watch.transitionTo(new RunningState());
    }

    @Override
    public void stop(StopWatch watch) {
        watch.transitionTo(new IdleState());
    }

    @Override
    public String name() {
        return "PAUSED";
    }
}
