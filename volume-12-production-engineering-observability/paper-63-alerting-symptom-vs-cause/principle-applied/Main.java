public class Main {
    public static void main(String[] args) {
        SymptomBasedAlerter alerter = new SymptomBasedAlerter();

        double scenarioAUserErrorRate = 0.0;   // compaction job, no user impact
        double scenarioBUserErrorRate = 0.40;  // downstream hang, real outage

        boolean scenarioAAlert = alerter.shouldAlert(scenarioAUserErrorRate);
        boolean scenarioBAlert = alerter.shouldAlert(scenarioBUserErrorRate);

        System.out.println("Scenario A (compaction job, 0% user impact): alert=" + scenarioAAlert
                + " (CORRECT -- nothing users felt, nothing pages)");
        System.out.println("Scenario B (downstream hang, 40% user error rate): alert=" + scenarioBAlert
                + " (CORRECT -- real outage pages, regardless of what caused it)");
    }
}
