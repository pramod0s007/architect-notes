public class Main {
    private static final int REQUESTS_PER_MINUTE = 1000;
    private static final int WINDOW_MINUTES = 1000;
    private static final double SLO_TARGET = 0.99;
    private static final int TOTAL_BUDGET = (int) (REQUESTS_PER_MINUTE * WINDOW_MINUTES * (1 - SLO_TARGET));

    public static void main(String[] args) {
        runSlowBurnScenario();
        runBriefBlipScenario();
    }

    private static void runSlowBurnScenario() {
        BurnRateAlerter alerter = new BurnRateAlerter(TOTAL_BUDGET, WINDOW_MINUTES);
        int errorsPerMinute = (int) (REQUESTS_PER_MINUTE * 0.02); // sustained 2%, double the sustainable rate

        int alertMinute = -1;
        for (int minute = 1; minute <= WINDOW_MINUTES; minute++) {
            if (alerter.recordMinuteAndCheck(errorsPerMinute) && alertMinute == -1) {
                alertMinute = minute;
                break;
            }
        }
        System.out.println("Slow burn (sustained 2% error rate): burn-rate alerter triggered at minute "
                + alertMinute + " -- " + (WINDOW_MINUTES - alertMinute) + " minutes of runway to react.");
    }

    private static void runBriefBlipScenario() {
        BurnRateAlerter alerter = new BurnRateAlerter(TOTAL_BUDGET, WINDOW_MINUTES);
        int healthyErrorsPerMinute = (int) (REQUESTS_PER_MINUTE * 0.005); // 0.5%, well within budget
        int blipErrorsPerMinute = (int) (REQUESTS_PER_MINUTE * 0.15);    // a 2-minute deploy hiccup at 15%

        int alertMinute = -1;
        for (int minute = 1; minute <= WINDOW_MINUTES; minute++) {
            int errorsThisMinute = (minute == 100 || minute == 101) ? blipErrorsPerMinute : healthyErrorsPerMinute;
            if (alerter.recordMinuteAndCheck(errorsThisMinute) && alertMinute == -1) {
                alertMinute = minute;
            }
        }
        System.out.println("Brief blip (2 minutes at 15%, otherwise healthy 0.5%): burn-rate alerter triggered at minute "
                + (alertMinute == -1 ? "NEVER" : alertMinute) + " -- correctly recognizes this doesn't threaten the budget.");
    }
}
