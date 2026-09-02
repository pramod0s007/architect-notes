class Service {
    final String name;
    final String shape; // "HTTP_STANDARD" or "BATCH_JOB"
    final boolean hasHealthCheck;
    final boolean hasLogging;

    Service(String name, String shape, boolean hasHealthCheck, boolean hasLogging) {
        this.name = name;
        this.shape = shape;
        this.hasHealthCheck = hasHealthCheck;
        this.hasLogging = hasLogging;
    }

    boolean isBaselineCompliant() {
        return hasHealthCheck && hasLogging;
    }
}
