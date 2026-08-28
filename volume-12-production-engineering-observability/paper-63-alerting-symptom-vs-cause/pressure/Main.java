public class Main {
    public static void main(String[] args) {
        CauseBasedAlerter alerter = new CauseBasedAlerter();

        // Scenario A: a nightly compaction job pins CPU at 95%. Every
        // customer request still succeeds, at normal latency.
        double scenarioACpu = 0.95;
        double scenarioAUserErrorRate = 0.0;
        boolean scenarioAAlert = alerter.shouldAlert(scenarioACpu);

        // Scenario B: a downstream dependency hangs. Requests pile up
        // waiting on it -- threads are BLOCKED, not computing, so CPU
        // actually stays low. 40% of customer requests time out.
        double scenarioBCpu = 0.20;
        double scenarioBUserErrorRate = 0.40;
        boolean scenarioBAlert = alerter.shouldAlert(scenarioBCpu);

        System.out.println("Scenario A (compaction job, 95% CPU, 0% user impact): alert=" + scenarioAAlert
                + " (WRONG -- pages for something users never felt)");
        System.out.println("Scenario B (downstream hang, 20% CPU, 40% user error rate): alert=" + scenarioBAlert
                + " (WRONG -- silent during a real outage)");
    }
}
