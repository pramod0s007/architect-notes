// Fixed: the return type has room for what callers actually need to know.
interface PaymentGateway {
    SettlementStatus charge(double amount);
}
