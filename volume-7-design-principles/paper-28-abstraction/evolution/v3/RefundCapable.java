interface RefundCapable {
    boolean refundPartial(String transactionId, double amount, String reason);
}
