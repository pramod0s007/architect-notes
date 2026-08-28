import java.util.List;

// Lists every contributing factor anyone can think of -- genuinely more
// documentation than the single-root-cause version. Nothing distinguishes
// which ones are actually critical, so remediation happens in whatever
// order the factors were listed, until the team's capacity runs out.
class UnprioritizedTimeline {
    void conduct(List<ContributingFactor> factors, int remediationCapacity) {
        int remediated = 0;
        for (ContributingFactor f : factors) {
            if (remediated >= remediationCapacity) {
                break;
            }
            f.remediated = true;
            remediated++;
        }
    }
}
