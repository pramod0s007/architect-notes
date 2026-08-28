import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<ContributingFactor> factors = new ArrayList<>();
        factors.add(new ContributingFactor("deploy skipped the canary stage", true));
        factors.add(new ContributingFactor("alert threshold was too high to catch this class of failure", true));
        factors.add(new ContributingFactor("on-call runbook was six months out of date", false));

        SingleRootCausePostmortem postmortem = new SingleRootCausePostmortem();
        // The most obvious, most proximate cause gets picked -- a completely
        // natural, common choice for "what caused this."
        postmortem.conduct(factors, "deploy skipped the canary stage");

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
            System.out.println("A later, unrelated deploy issue occurs. The alert threshold -- never fixed,");
            System.out.println("because it wasn't THE root cause -- fails to catch it again.");
            System.out.println("INCIDENT RECURS: " + unremediatedCritical + " critical factor(s) were left unaddressed.");
        } else {
            System.out.println("No critical factors remain unaddressed.");
        }
    }
}
