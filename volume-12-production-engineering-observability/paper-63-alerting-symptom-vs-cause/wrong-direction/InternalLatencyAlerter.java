// Still "symptom-based" in name -- it measures a latency number, not an
// internal resource. The mistake is WHERE it measures: how long the app
// server takes to process a request it has already started handling,
// not how long the customer actually waited for a response.
class InternalLatencyAlerter {
    private static final double LATENCY_THRESHOLD_MS = 200;

    boolean shouldAlert(double internalProcessingTimeMs) {
        return internalProcessingTimeMs > LATENCY_THRESHOLD_MS;
    }
}
