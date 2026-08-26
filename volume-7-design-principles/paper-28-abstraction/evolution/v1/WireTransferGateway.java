// Settlement actually takes 1-3 business days. The interface has no room to say "pending",
// so this implementation lies and returns true immediately.
class WireTransferGateway implements PaymentGateway {
    public boolean charge(double amount) {
        return true; // true-and-hope: the incident waiting to happen
    }
}
