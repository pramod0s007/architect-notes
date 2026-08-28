class LegacyPaymentGateway {
    static boolean charge(String customerId, double amount) {
        System.out.println("[legacy] charged " + customerId + " $" + amount);
        return true;
    }

    static void refund(String customerId, double amount) {
        System.out.println("[legacy] refunded " + customerId + " $" + amount);
    }
}
