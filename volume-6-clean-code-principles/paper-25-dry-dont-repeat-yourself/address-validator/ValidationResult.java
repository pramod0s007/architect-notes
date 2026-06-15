import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carries the outcome of an address validation: pass/fail + collected error messages.
 */
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(errors);
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult failed(List<String> errors) {
        return new ValidationResult(false, new ArrayList<>(errors));
    }

    public boolean isValid()          { return valid; }
    public List<String> getErrors()   { return errors; }

    @Override
    public String toString() {
        return valid ? "VALID" : "INVALID: " + errors;
    }
}
