import java.util.List;

// No single "root cause" field to fill in. Every factor that genuinely
// contributed gets tracked, and every CRITICAL one gets a remediation --
// not just whichever one felt most like "the" cause.
class ContributingFactorsTimeline {
    void conduct(List<ContributingFactor> factors) {
        for (ContributingFactor f : factors) {
            if (f.critical) {
                f.remediated = true;
            }
        }
    }
}
