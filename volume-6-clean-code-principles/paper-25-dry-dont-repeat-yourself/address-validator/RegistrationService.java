/**
 * Delegates address validation to AddressValidator.
 *
 * Before DRY: this class contained its own postal-code regex and city checks.
 * A bug fix in one copy never made it to checkout or shipping.
 */
public class RegistrationService {

    private final AddressValidator validator;

    public RegistrationService(AddressValidator validator) {
        this.validator = validator;
    }

    public void registerUser(String name, Address address) {
        ValidationResult result = validator.validate(address);
        if (!result.isValid()) {
            System.out.println("Registration failed for " + name + ": " + result.getErrors());
            return;
        }
        System.out.println("User '" + name + "' registered at: " + address);
    }
}
