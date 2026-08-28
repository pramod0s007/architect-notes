// Doesn't ask "is the error rate high right now" -- asks "at the rate we're
// consuming the error budget, will it run out before the window ends."
// The rate is estimated from the FULL elapsed history, not a short recent
// slice, so a brief blip doesn't distort it much.
class BurnRateAlerter {
    private static final int MIN_EVALUATION_MINUTES = 15; // don't page on a single noisy data point

    private final int totalBudget;
    private final int windowMinutes;

    private int cumulativeErrors = 0;
    private int elapsedMinutes = 0;

    BurnRateAlerter(int totalBudget, int windowMinutes) {
        this.totalBudget = totalBudget;
        this.windowMinutes = windowMinutes;
    }

    boolean recordMinuteAndCheck(int errorsThisMinute) {
        cumulativeErrors += errorsThisMinute;
        elapsedMinutes++;

        if (elapsedMinutes < MIN_EVALUATION_MINUTES || cumulativeErrors == 0) {
            return false;
        }

        double currentRatePerMinute = (double) cumulativeErrors / elapsedMinutes;
        int remainingBudget = totalBudget - cumulativeErrors;
        int remainingMinutes = windowMinutes - elapsedMinutes;

        if (remainingBudget <= 0) {
            return true; // budget is already gone
        }

        double projectedMinutesToExhaustion = remainingBudget / currentRatePerMinute;
        return projectedMinutesToExhaustion < remainingMinutes;
    }
}
