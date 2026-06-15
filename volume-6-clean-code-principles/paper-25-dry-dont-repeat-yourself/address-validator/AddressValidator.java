import java.util.ArrayList;
import java.util.List;

/**
 * DRY: single source of truth for all address validation rules.
 *
 * Before DRY: registration, checkout, and shipping each had their own
 * copy of isValidPostalCode(), diverging silently as requirements changed.
 * After DRY: one fix here propagates to every caller automatically.
 */
public class AddressValidator {

    public boolean isValidPostalCode(String code) {
        return code != null && code.matches("\\d{5}(-\\d{4})?");
    }

    public boolean isValidCity(String city) {
        return city != null && !city.isBlank() && city.length() <= 100;
    }

    public boolean isValidStreet(String street) {
        return street != null && !street.isBlank() && street.length() >= 5;
    }

    public ValidationResult validate(Address address) {
        List<String> errors = new ArrayList<>();
        if (!isValidStreet(address.getStreet())) {
            errors.add("Street must be at least 5 characters");
        }
        if (!isValidCity(address.getCity())) {
            errors.add("City must not be blank and under 100 characters");
        }
        if (!isValidPostalCode(address.getPostalCode())) {
            errors.add("Postal code must be 5 digits or ZIP+4 format");
        }
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.failed(errors);
    }
}
