import java.util.Map;

/**
 * A contract is authored and owned by the consumer, not the provider --
 * it encodes what that specific consumer's own code actually does with a
 * response, so it can never miss a real dependency the provider doesn't
 * know about. Each contract also declares which order scenario it needs to
 * be checked against (e.g. "SHIPPED" or "CANCELLED"), so the runner always
 * feeds it a response of the kind its own consumer actually receives. The
 * provider's build runs every registered consumer's contract, against its
 * own declared scenario, before allowing a deploy.
 */
public interface ConsumerContract {
    String consumerName();

    /** Which order scenario this contract needs to be checked against. */
    String scenarioKey();

    /** Throws ContractViolationException if the response would break this consumer's real behavior. */
    void verify(Map<String, Object> response) throws ContractViolationException;
}
