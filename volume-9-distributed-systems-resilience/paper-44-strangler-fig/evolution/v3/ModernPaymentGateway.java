class ModernPaymentGateway {
    static boolean charge(String customerId, double amount) {
        System.out.println("[modern] charged " + customerId + " $" + amount);
        return true;
    }
}
