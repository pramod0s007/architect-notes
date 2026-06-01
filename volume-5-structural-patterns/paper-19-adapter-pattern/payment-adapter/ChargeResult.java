/**
 * Normalised result of a payment charge operation.
 */
public class ChargeResult {

    private final boolean success;
    private final String transactionId;
    private final String errorMessage;

    private ChargeResult(boolean success, String transactionId, String errorMessage) {
        this.success       = success;
        this.transactionId = transactionId;
        this.errorMessage  = errorMessage;
    }

    public static ChargeResult succeeded(String transactionId) {
        return new ChargeResult(true, transactionId, null);
    }

    public static ChargeResult failed(String errorMessage) {
        return new ChargeResult(false, null, errorMessage);
    }

    public boolean isSuccess()         { return success; }
    public String  getTransactionId()  { return transactionId; }
    public String  getErrorMessage()   { return errorMessage; }

    @Override
    public String toString() {
        return success
               ? "ChargeResult{success=true, transactionId='" + transactionId + "'}"
               : "ChargeResult{success=false, error='" + errorMessage + "'}";
    }
}
