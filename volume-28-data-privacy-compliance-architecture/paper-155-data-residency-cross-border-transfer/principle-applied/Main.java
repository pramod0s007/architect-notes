/**
 * PRINCIPLE APPLIED: this sample models a single residency-decision gate that any caller -- a
 * write path or a downstream pipeline -- can call before moving a customer's data across zones.
 * The gate's default is deny: a raw record may not cross into a different zone from the
 * customer's residency zone unless a non-blank transfer approval reference is present. Two things
 * are allowed without an approval: a transfer that never actually crosses zones (the destination
 * matches the customer's own), and a transfer of a non-identifying aggregate (a count, not raw
 * records) -- because aggregate statistics don't carry the same residency requirement as an
 * individual's raw record. This sample only checks that an approval reference is a non-blank
 * string; it does not validate that the reference names a real, current, in-scope legal transfer
 * instrument, and it only demonstrates the decision logic itself, not a guarantee that every
 * consumer in a real system is wired to call it -- that structural enforcement is a separate,
 * necessary piece this sample doesn't build.
 */
public class Main {
    public static void main(String[] args) {
        CustomerRecord euCustomer = new CustomerRecord("customer-301", "EU");
        CustomerRecord usCustomer = new CustomerRecord("customer-302", "US");

        ResidencyGate gate = new ResidencyGate();

        System.out.println("-- Case 1: EU customer, local EU analytics query (same zone) --");
        evaluate(gate, euCustomer, new TransferRequest("EU", true, null));

        System.out.println();
        System.out.println("-- Case 2: EU customer, raw record requested for us-east warehouse, no approval --");
        evaluate(gate, euCustomer, new TransferRequest("US", true, null));

        System.out.println();
        System.out.println("-- Case 3: EU customer, aggregate-only stats requested for us-east dashboard --");
        evaluate(gate, euCustomer, new TransferRequest("US", false, null));

        System.out.println();
        System.out.println("-- Case 4: EU customer, raw record requested with an approval reference on file --");
        evaluate(gate, euCustomer, new TransferRequest("US", true, "SCC-2026-0091"));

        System.out.println();
        System.out.println("-- Case 5: US customer, local US analytics query (same zone) --");
        evaluate(gate, usCustomer, new TransferRequest("US", true, null));

        System.out.println();
        System.out.println("-- Case 6: EU customer, raw record requested with a blank approval reference --");
        evaluate(gate, euCustomer, new TransferRequest("US", true, "  "));
    }

    private static void evaluate(ResidencyGate gate, CustomerRecord customer, TransferRequest request) {
        TransferDecision decision = gate.evaluate(customer, request);
        String verb = decision.allowed ? "ALLOWED" : "DENIED";
        System.out.println(verb + ": transfer of " + customer.customerId + "'s data "
                + "(residency zone: " + customer.residencyZone + ") to destination zone "
                + request.destinationZone + " -- " + decision.reason);
    }

    static class ResidencyGate {
        TransferDecision evaluate(CustomerRecord customer, TransferRequest request) {
            if (request.destinationZone.equals(customer.residencyZone)) {
                return new TransferDecision(true, "destination matches the customer's own modeled "
                        + "residency zone, so this sample treats it as an in-zone transfer under "
                        + "its own zone model");
            }
            if (!request.rawRecordRequested) {
                return new TransferDecision(true, "only a non-identifying aggregate was requested, "
                        + "which does not carry this customer's individual residency requirement");
            }
            if (request.transferApprovalId == null || request.transferApprovalId.isBlank()) {
                return new TransferDecision(false, "raw record requested across zones with no "
                        + "transfer approval on file -- default is deny");
            }
            return new TransferDecision(true, "raw record transfer allowed because a non-blank "
                    + "approval reference is on file (" + request.transferApprovalId + ") -- this "
                    + "sample checks only that a reference exists, not that it is a valid, current, "
                    + "in-scope legal transfer instrument");
        }
    }

    static class TransferDecision {
        boolean allowed;
        String reason;

        TransferDecision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }
    }

    static class TransferRequest {
        String destinationZone;
        boolean rawRecordRequested;
        String transferApprovalId;

        TransferRequest(String destinationZone, boolean rawRecordRequested, String transferApprovalId) {
            this.destinationZone = destinationZone;
            this.rawRecordRequested = rawRecordRequested;
            this.transferApprovalId = transferApprovalId;
        }
    }

    static class CustomerRecord {
        String customerId;
        String residencyZone;

        CustomerRecord(String customerId, String residencyZone) {
            this.customerId = customerId;
            this.residencyZone = residencyZone;
        }
    }
}
