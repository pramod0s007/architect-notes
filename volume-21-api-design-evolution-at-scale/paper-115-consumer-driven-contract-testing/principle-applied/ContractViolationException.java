/** Thrown when a candidate response would violate a consumer's own contract. */
public class ContractViolationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ContractViolationException(String message) {
        super(message);
    }
}
