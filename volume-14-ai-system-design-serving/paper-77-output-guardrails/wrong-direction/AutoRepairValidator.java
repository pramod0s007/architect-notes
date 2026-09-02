// The overcorrection: instead of rejecting invalid output, "helpfully" repair
// it so processing can continue -- a negative amount gets its sign flipped
// rather than the request being flagged. This looks like it recovered a usable
// request. It actually let a corrupted, unverified value straight through.
class AutoRepairValidator {
    static double repairedAmount(RefundRequest r) {
        return r.amount < 0 ? Math.abs(r.amount) : r.amount;
    }
}
