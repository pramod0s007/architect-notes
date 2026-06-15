/**
 * DRY — address-validator
 *
 * One AddressValidator instance shared across three services.
 * Change the postal-code rule once — registration, checkout, and shipping
 * all reflect it immediately with no code duplication.
 */
public class Main {

    public static void main(String[] args) {
        AddressValidator validator = new AddressValidator();
        RegistrationService registration = new RegistrationService(validator);
        CheckoutService checkout         = new CheckoutService(validator);
        ShippingService shipping         = new ShippingService(validator);

        Address valid = new Address("123 Elm Street", "Springfield", "62701", "US");
        Address badPostal  = new Address("456 Oak Avenue", "Shelbyville", "ABCDE", "US");
        Address blankCity  = new Address("789 Pine Road", "", "10001", "US");

        System.out.println("=== Valid address across all services ===");
        registration.registerUser("Alice", valid);
        checkout.placeOrder("user-42", valid);
        shipping.scheduleDelivery("order-99", valid);

        System.out.println();
        System.out.println("=== Bad postal code — same rule, three services ===");
        registration.registerUser("Bob", badPostal);
        checkout.placeOrder("user-43", badPostal);
        shipping.scheduleDelivery("order-100", badPostal);

        System.out.println();
        System.out.println("=== Blank city ===");
        registration.registerUser("Carol", blankCity);
    }
}
