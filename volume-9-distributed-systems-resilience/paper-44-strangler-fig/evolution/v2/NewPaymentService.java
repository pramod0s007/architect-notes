class NewPaymentService {
    boolean charge(String customerId, double amount) {
        return ModernPaymentGateway.charge(customerId, amount);
    }
}
