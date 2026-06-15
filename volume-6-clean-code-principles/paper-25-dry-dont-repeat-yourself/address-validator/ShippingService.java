/**
 * Delegates address validation to AddressValidator.
 *
 * Third consumer of the same validation rules — no copy-paste.
 * Update the validator once; all three services stay consistent.
 */
public class ShippingService {

    private final AddressValidator validator;

    public ShippingService(AddressValidator validator) {
        this.validator = validator;
    }

    public void scheduleDelivery(String orderId, Address deliveryAddress) {
        ValidationResult result = validator.validate(deliveryAddress);
        if (!result.isValid()) {
            System.out.println("Delivery for order " + orderId + " blocked: " + result.getErrors());
            return;
        }
        System.out.println("Delivery scheduled for order " + orderId + " to: " + deliveryAddress);
    }
}
