class Service {
    final String name;
    final String shape; // "HTTP_STANDARD" or "BATCH_JOB"
    final boolean usesEscapeHatch;
    final boolean hasHealthCheck;
    final boolean hasLogging;

    Service(String name, String shape, boolean usesEscapeHatch, boolean hasHealthCheck, boolean hasLogging) {
        this.name = name;
        this.shape = shape;
        this.usesEscapeHatch = usesEscapeHatch;
        this.hasHealthCheck = hasHealthCheck;
        this.hasLogging = hasLogging;
    }

    boolean isBaselineCompliant() {
        return hasHealthCheck && hasLogging;
    }
}
