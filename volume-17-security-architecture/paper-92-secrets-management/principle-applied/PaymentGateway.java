class PaymentGateway {
    static boolean authorizeCharge(SecretStore store, String environment, String providedKey, int amountCents) {
        String realSecret = store.get(environment);
        return realSecret != null && realSecret.equals(providedKey);
    }
}
