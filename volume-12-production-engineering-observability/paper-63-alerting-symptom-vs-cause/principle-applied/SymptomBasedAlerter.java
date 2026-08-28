// Watches what users actually experience -- error rate as seen at the
// point requests return to the customer. Doesn't care why it happened,
// only whether it happened.
class SymptomBasedAlerter {
    private static final double ERROR_RATE_THRESHOLD = 0.05;

    boolean shouldAlert(double userFacingErrorRate) {
        return userFacingErrorRate > ERROR_RATE_THRESHOLD;
    }
}
