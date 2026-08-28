import java.util.List;

// Forces the process to name exactly ONE root cause. Whichever factor gets
// that label is the only one that gets a remediation -- everything else
// that genuinely contributed stays exactly as it was.
class SingleRootCausePostmortem {
    void conduct(List<ContributingFactor> factors, String chosenRootCause) {
        for (ContributingFactor f : factors) {
            if (f.description.equals(chosenRootCause)) {
                f.remediated = true;
            }
        }
    }
}
