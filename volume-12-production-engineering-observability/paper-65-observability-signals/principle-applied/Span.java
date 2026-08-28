class Span {
    final String serviceName;
    final long durationMs;

    Span(String serviceName, long durationMs) {
        this.serviceName = serviceName;
        this.durationMs = durationMs;
    }
}
