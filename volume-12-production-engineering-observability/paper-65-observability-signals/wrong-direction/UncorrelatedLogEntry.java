// A log line from ServiceC, on its own, in a service with several requests
// in flight at once. No field ties it back to which Gateway request caused it.
class UncorrelatedLogEntry {
    final long startTimeMs;
    final long durationMs;

    UncorrelatedLogEntry(long startTimeMs, long durationMs) {
        this.startTimeMs = startTimeMs;
        this.durationMs = durationMs;
    }
}
