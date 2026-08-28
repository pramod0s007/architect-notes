class LegacyPaymentGateway {
    static boolean charge(String customerId, double amount) {
        if (customerId.equals("CUST-DECLINE")) {
            return false; // simulates a real decline from the payment provider
        }
        return true;
    }
}
