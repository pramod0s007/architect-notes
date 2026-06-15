// Shared stub for evolution examples
public class Customer {
    private final String email;
    private final String region;

    public Customer(String email, String region) {
        this.email = email; this.region = region;
    }

    public String getEmail()         { return email; }
    public String getRegion()        { return region; }
    public String getPaymentMethod() { return "CREDIT_CARD"; }  // stub for evolution examples
}
