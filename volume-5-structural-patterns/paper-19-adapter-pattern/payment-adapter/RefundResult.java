/**
 * Normalised result of a refund operation.
 */
public class RefundResult {

    private final boolean success;
    private final String refundId;
    private final String errorMessage;

    private RefundResult(boolean success, String refundId, String errorMessage) {
        this.success      = success;
        this.refundId     = refundId;
        this.errorMessage = errorMessage;
    }

    public static RefundResult succeeded(String refundId) {
        return new RefundResult(true, refundId, null);
    }

    public static RefundResult failed(String errorMessage) {
        return new RefundResult(false, null, errorMessage);
    }

    public boolean isSuccess()        { return success; }
    public String  getRefundId()      { return refundId; }
    public String  getErrorMessage()  { return errorMessage; }

    @Override
    public String toString() {
        return success
               ? "RefundResult{success=true, refundId='" + refundId + "'}"
               : "RefundResult{success=false, error='" + errorMessage + "'}";
    }
}
