import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<ContributingFactor> factors = new ArrayList<>();
        factors.add(new ContributingFactor("deploy skipped the canary stage", true));
        factors.add(new ContributingFactor("on-call engineer was new to the team", false));
        factors.add(new ContributingFactor("incident happened during a holiday", false));
        factors.add(new ContributingFactor("the metrics dashboard was slow to load", false));
        factors.add(new ContributingFactor("the postmortem doc template was confusing", false));
        factors.add(new ContributingFactor("a flaky CI test delayed the fix rollout", false));
        factors.add(new ContributingFactor("alert threshold was too high to catch this class of failure", true));
        factors.add(new ContributingFactor("on-call runbook was six months out of date", false));

        UnprioritizedTimeline postmortem = new UnprioritizedTimeline();
        int teamCapacityThisSprint = 3; // a realistic number of remediation items a team can actually ship
        postmortem.conduct(factors, teamCapacityThisSprint);

        System.out.println("Postmortem complete. " + factors.size() + " factors documented -- more than the single-root-cause version's 1.");
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
            System.out.println("A later, unrelated deploy issue occurs. The alert threshold -- documented,");
            System.out.println("but never prioritized above the other " + (factors.size() - 1) + " factors -- fails to catch it again.");
            System.out.println("INCIDENT RECURS: " + unremediatedCritical + " critical factor(s) were left unaddressed, despite more total work being done.");
        } else {
            System.out.println("No critical factors remain unaddressed.");
        }
    }
}
