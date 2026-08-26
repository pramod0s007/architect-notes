class RefundService {
    void refund(PaymentGateway gateway, double amount) {
        // The caller has no way to know settlement hasn't actually happened —
        // the interface told it "true" means done.
        System.out.println("Processing refund of " + amount + " against a payment reported as settled.");
    }
}
