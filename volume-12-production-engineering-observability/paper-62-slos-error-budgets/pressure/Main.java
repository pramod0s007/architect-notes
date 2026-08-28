public class Main {
    private static final int REQUESTS_PER_MINUTE = 1000;
    private static final int WINDOW_MINUTES = 1000; // a compressed stand-in for a 30-day SLO window
    private static final double SLO_TARGET = 0.99;   // 99% success required
    private static final int TOTAL_BUDGET = (int) (REQUESTS_PER_MINUTE * WINDOW_MINUTES * (1 - SLO_TARGET));

    public static void main(String[] args) {
        NaiveThresholdAlerter alerter = new NaiveThresholdAlerter();

        // A bug ships that doubles the sustainable error rate: 2% instead of
        // the 1% the SLO can tolerate, and it just... stays there.
        double sustainedErrorRate = 0.02;
        int errorsInWindow = (int) (REQUESTS_PER_MINUTE * sustainedErrorRate);

        int cumulativeErrors = 0;
        int alertMinute = -1;
        for (int minute = 1; minute <= WINDOW_MINUTES; minute++) {
            cumulativeErrors += errorsInWindow;
            if (alerter.shouldAlert(REQUESTS_PER_MINUTE, errorsInWindow) && alertMinute == -1) {
                alertMinute = minute;
            }
            if (cumulativeErrors >= TOTAL_BUDGET && minute <= WINDOW_MINUTES) {
                System.out.println("Entire " + WINDOW_MINUTES + "-minute error budget (" + TOTAL_BUDGET
                        + " errors) exhausted by minute " + minute + " -- with " + (WINDOW_MINUTES - minute)
                        + " minutes still left in the window.");
                break;
            }
        }

        System.out.printf("Sustained error rate: %.1f%% (SLO allows %.1f%%)%n", sustainedErrorRate * 100, (1 - SLO_TARGET) * 100);
        System.out.println("Naive threshold alerter (fires above 10% in a short window) triggered at minute: "
                + (alertMinute == -1 ? "NEVER" : alertMinute));
    }
}
