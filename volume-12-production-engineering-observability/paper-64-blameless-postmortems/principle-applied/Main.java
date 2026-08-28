import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<ContributingFactor> factors = new ArrayList<>();
        factors.add(new ContributingFactor("deploy skipped the canary stage", true));
        factors.add(new ContributingFactor("alert threshold was too high to catch this class of failure", true));
        factors.add(new ContributingFactor("on-call runbook was six months out of date", false));

        ContributingFactorsTimeline postmortem = new ContributingFactorsTimeline();
        postmortem.conduct(factors);

        System.out.println("Postmortem complete. Remediation status:");
        int unremediatedCritical = 0;
        for (ContributingFactor f : factors) {
            System.out.println("  [" + (f.remediated ? "FIXED" : "not fixed") + "] " + f.description
                    + (f.critical ? " (critical)" : ""));
            if (f.critical && !f.remediated) {
                unremediatedCritical++;
            }
        }

        System.out.println();
        if (unremediatedCritical > 0) {
            System.out.println("INCIDENT RECURS: " + unremediatedCritical + " critical factor(s) were left unaddressed.");
        } else {
            System.out.println("A later, unrelated deploy issue occurs. This time the alert threshold catches it.");
            System.out.println("No critical factors remain unaddressed -- the incident does not recur.");
        }
    }
}
