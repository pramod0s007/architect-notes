// Validates a model-generated refund request against the schema it's actually
// required to satisfy before it's allowed to move money. Anything that fails
// is REJECTED -- never silently adjusted into something that happens to pass.
class RefundValidator {
    static boolean isValid(RefundRequest r) {
        if (r.orderId == null || r.reason == null) {
            return false;
        }
        if (r.amount <= 0) {
            return false;
        }
        if (r.amount > r.orderMaxRefundable) {
            return false;
        }
        return true;
    }
}
