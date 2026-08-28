// Measures the same kind of thing -- latency -- but at the point closest
// to the customer: queue time plus processing time, the full duration a
// request actually took from the customer's perspective.
class EndToEndLatencyAlerter {
    private static final double LATENCY_THRESHOLD_MS = 200;

    boolean shouldAlert(double queueTimeMs, double internalProcessingTimeMs) {
        double endToEndMs = queueTimeMs + internalProcessingTimeMs;
        return endToEndMs > LATENCY_THRESHOLD_MS;
    }
}
