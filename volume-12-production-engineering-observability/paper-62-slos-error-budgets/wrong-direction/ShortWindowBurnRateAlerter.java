import java.util.ArrayDeque;
import java.util.Deque;

// Same burn-rate math as BurnRateAlerter -- projected time to exhaustion vs.
// remaining window. The rate is estimated from only the last 5 minutes
// instead of the full elapsed history, to "react faster."
class ShortWindowBurnRateAlerter {
    private static final int SHORT_WINDOW_MINUTES = 5;

    private final int totalBudget;
    private final int windowMinutes;
    private final Deque<Integer> recentErrors = new ArrayDeque<>();

    private int cumulativeErrors = 0;
    private int elapsedMinutes = 0;

    ShortWindowBurnRateAlerter(int totalBudget, int windowMinutes) {
        this.totalBudget = totalBudget;
        this.windowMinutes = windowMinutes;
    }

    boolean recordMinuteAndCheck(int errorsThisMinute) {
        cumulativeErrors += errorsThisMinute;
        elapsedMinutes++;

        recentErrors.addLast(errorsThisMinute);
        if (recentErrors.size() > SHORT_WINDOW_MINUTES) {
            recentErrors.removeFirst();
        }

        if (elapsedMinutes < SHORT_WINDOW_MINUTES) {
            return false;
        }

        int recentSum = 0;
        for (int e : recentErrors) recentSum += e;
        double currentRatePerMinute = (double) recentSum / recentErrors.size();

        int remainingBudget = totalBudget - cumulativeErrors;
        int remainingMinutes = windowMinutes - elapsedMinutes;

        if (remainingBudget <= 0) {
            return true;
        }
        if (currentRatePerMinute == 0) {
            return false;
        }

        double projectedMinutesToExhaustion = remainingBudget / currentRatePerMinute;
        return projectedMinutesToExhaustion < remainingMinutes;
    }
}
