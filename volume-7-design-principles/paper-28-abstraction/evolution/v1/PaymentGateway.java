// The abstraction promises less than reality requires: true = ??? (success? pending? unknown)
interface PaymentGateway {
    boolean charge(double amount);
}
