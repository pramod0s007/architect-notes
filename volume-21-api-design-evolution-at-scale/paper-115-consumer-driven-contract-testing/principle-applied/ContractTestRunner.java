import java.util.List;
import java.util.Map;

/**
 * Runs every registered consumer contract against the candidate response
 * for its own declared scenario, before a deploy is allowed to proceed.
 * This is the mechanism that turns "the provider guesses what matters"
 * into "every real consumer states what matters, and the provider's own
 * build enforces it, against the exact scenario that consumer actually
 * depends on."
 */
public class ContractTestRunner {
    private final List<ConsumerContract> contracts;

    public ContractTestRunner(List<ConsumerContract> contracts) {
        this.contracts = contracts;
    }

    public boolean runAll(Map<String, Map<String, Object>> candidateResponsesByScenario) {
        boolean allPassed = true;
        for (ConsumerContract contract : contracts) {
            Map<String, Object> response = candidateResponsesByScenario.get(contract.scenarioKey());
            try {
                contract.verify(response);
                System.out.println("  [" + contract.consumerName() + ", scenario=" + contract.scenarioKey()
                        + "] PASS");
            } catch (ContractViolationException e) {
                System.out.println("  [" + contract.consumerName() + ", scenario=" + contract.scenarioKey()
                        + "] FAIL: " + e.getMessage());
                allPassed = false;
            }
        }
        return allPassed;
    }
}
