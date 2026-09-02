// The model's structured output for a refund action, already parsed into a Java
// object -- however that parsing happened is out of scope for this paper. The
// question this paper is about is what happens to this object AFTER parsing,
// before it's allowed to actually move money.
class RefundRequest {
    final String orderId;
    final double amount;
    final String reason;
    final double orderMaxRefundable;

    RefundRequest(String orderId, double amount, String reason, double orderMaxRefundable) {
        this.orderId = orderId;
        this.amount = amount;
        this.reason = reason;
        this.orderMaxRefundable = orderMaxRefundable;
    }
}
