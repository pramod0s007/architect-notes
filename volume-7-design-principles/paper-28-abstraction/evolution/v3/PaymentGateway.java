// Fixed: the base abstraction covers only what every payment method truly has.
interface PaymentGateway {
    SettlementStatus charge(double amount);
}
