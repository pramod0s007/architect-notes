// Routing rule duplicated here. Updated this sprint to also route EU customers
// to the modern provider -- but only here.
class OrderService {
    boolean charge(String customerId, double amount) {
        if (customerId.startsWith("INTL-") || customerId.endsWith("-EU")) {
            System.out.println("[modern] charged " + customerId + " $" + amount);
            return true;
        }
        System.out.println("[legacy] charged " + customerId + " $" + amount);
        return true;
    }
}
