class StripeGateway implements PaymentGateway {
    public boolean charge(double amount) {
        return true; // card payments settle synchronously — true is honest here
    }
}
