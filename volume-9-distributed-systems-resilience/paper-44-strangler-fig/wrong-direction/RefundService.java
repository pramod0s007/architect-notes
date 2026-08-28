// Same routing rule, copy-pasted here weeks earlier. Nobody updated this copy
// when OrderService's rule was extended to include EU customers.
class RefundService {
    boolean refund(String customerId, double amount) {
        if (customerId.startsWith("INTL-")) { // stale -- missing the "-EU" case
            System.out.println("[modern] refunded " + customerId + " $" + amount);
            return true;
        }
        System.out.println("[legacy] refunded " + customerId + " $" + amount);
        return true;
    }
}
