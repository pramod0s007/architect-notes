/**
 * Delegates address validation to AddressValidator.
 *
 * DRY ensures the same postal-code rule applies here as in registration
 * and shipping — zero duplication.
 */
public class CheckoutService {

    private final AddressValidator validator;

    public CheckoutService(AddressValidator validator) {
        this.validator = validator;
    }

    public void placeOrder(String userId, Address shippingAddress) {
        ValidationResult result = validator.validate(shippingAddress);
        if (!result.isValid()) {
            System.out.println("Order rejected for user " + userId + ": " + result.getErrors());
            return;
        }
        System.out.println("Order placed for user " + userId + ", shipping to: " + shippingAddress);
    }
}
