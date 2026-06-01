// v1_InlineRules.java
// LeaveApprovalService.canApprove() with 3 inline rules.
// This feels fine — readable, fits in one screen.

import java.time.LocalDate;

public class v1_InlineRules {

    // ─── Domain model ─────────────────────────────────────────────────────────

    static class LeaveRequest {
        final String employeeId;
        final LocalDate startDate;
        final LocalDate endDate;
        final int       daysRequested;
        final boolean   managerApproved;
        final int       leaveBalanceDays;

        LeaveRequest(String employeeId, LocalDate start, LocalDate end,
                     boolean managerApproved, int leaveBalanceDays) {
            this.employeeId      = employeeId;
            this.startDate       = start;
            this.endDate         = end;
            this.daysRequested   = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
            this.managerApproved = managerApproved;
            this.leaveBalanceDays = leaveBalanceDays;
        }
    }

    // ─── Service ──────────────────────────────────────────────────────────────

    static class LeaveApprovalService {

        /**
         * Returns true if the leave request can be approved.
         *
         * Rules (only 3 right now — fits comfortably here):
         *  1. Employee must have sufficient leave balance.
         *  2. No other approved leave overlaps this period (simplified: always false here).
         *  3. Manager must have pre-approved the request.
         */
        boolean canApprove(LeaveRequest request) {
            // Rule 1: Sufficient balance
            if (request.leaveBalanceDays < request.daysRequested) {
                System.out.println("  Rejected: insufficient leave balance ("
                    + request.leaveBalanceDays + " available, "
                    + request.daysRequested + " requested)");
                return false;
            }

            // Rule 2: No overlapping leave (in reality you'd query the DB;
            //         here we trust the flag on the request object)
            boolean hasOverlap = false; // simplified
            if (hasOverlap) {
                System.out.println("  Rejected: overlaps with existing approved leave");
                return false;
            }

            // Rule 3: Manager sign-off
            if (!request.managerApproved) {
                System.out.println("  Rejected: manager has not approved the request");
                return false;
            }

            System.out.println("  Approved");
            return true;
        }
    }

    public static void main(String[] args) {
        LeaveApprovalService service = new LeaveApprovalService();

        System.out.println("Request 1 (balance OK, manager OK):");
        service.canApprove(new LeaveRequest("E001",
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5), true, 10));

        System.out.println("Request 2 (balance insufficient):");
        service.canApprove(new LeaveRequest("E002",
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10), true, 5));

        System.out.println("Request 3 (no manager approval):");
        service.canApprove(new LeaveRequest("E003",
            LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 3), false, 15));

        System.out.println("\n--- 3 rules is fine. Wait for v2 to see why this won't scale. ---");
    }
}
