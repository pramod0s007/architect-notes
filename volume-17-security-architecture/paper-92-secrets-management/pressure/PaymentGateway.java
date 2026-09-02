// The API key is a compile-time literal, baked directly into the class file
// that gets checked into source control, cloned to every developer's laptop,
// and shipped to every environment -- dev, staging, and production alike.
class PaymentGateway {
    static final String API_KEY = "sk-live-4f9a2b7c1e6d8f3a";

    static boolean authorizeCharge(String providedKey, int amountCents) {
        return API_KEY.equals(providedKey);
    }
}
