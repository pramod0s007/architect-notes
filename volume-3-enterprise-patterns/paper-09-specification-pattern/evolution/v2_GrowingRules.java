// v2_GrowingRules.java
// HR added 5 more rules. canApprove() is now 60+ lines.
// Three teams (HR Policy, Payroll, Compliance) all edit this one method.
// Testing one rule requires constructing a full LeaveRequest that satisfies all others.
// Producing a rejection REASON requires duplicating the conditions outside canApprove().

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class v2_GrowingRules {

    // ─── Domain model (grown since v1) ────────────────────────────────────────

    static class LeaveRequest {
        final String    employeeId;
        final LocalDate startDate;
        final LocalDate endDate;
        final int       daysRequested;
        final boolean   managerApproved;
        final int       leaveBalanceDays;

        // New fields added by HR
        final int       employmentMonths;   // probation rule
        final int       consecutiveDaysThisYear; // consecutive-leave limit
        final boolean   isBlackoutDate;     // company blackout period
        final String    role;               // role-based limits (e.g., "CONTRACTOR")
        final boolean   medicalCertProvided;// required for >3 days

        LeaveRequest(String employeeId, LocalDate start, LocalDate end,
                     boolean managerApproved, int leaveBalanceDays,
                     int employmentMonths, int consecutiveDaysThisYear,
                     boolean isBlackoutDate, String role, boolean medicalCertProvided) {
            this.employeeId              = employeeId;
            this.startDate               = start;
            this.endDate                 = end;
            this.daysRequested           = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
            this.managerApproved         = managerApproved;
            this.leaveBalanceDays        = leaveBalanceDays;
            this.employmentMonths        = employmentMonths;
            this.consecutiveDaysThisYear = consecutiveDaysThisYear;
            this.isBlackoutDate          = isBlackoutDate;
            this.role                    = role;
            this.medicalCertProvided     = medicalCertProvided;
        }
    }

    // ─── Service — the method is now a wall of conditions ─────────────────────

    static class LeaveApprovalService {

        boolean canApprove(LeaveRequest request) {

            // --- Rule 1: Sufficient balance (Payroll team owns this) ---
            if (request.leaveBalanceDays < request.daysRequested) {
                return false;
            }

            // --- Rule 2: No overlapping leave (HR Ops owns this) ---
            boolean hasOverlap = false; // simplified
            if (hasOverlap) {
                return false;
            }

            // --- Rule 3: Manager approval (Line Manager owns this) ---
            if (!request.managerApproved) {
                return false;
            }

            // --- Rule 4: Probation period check (HR Policy owns this) ---
            // Employees in first 3 months cannot take more than 2 consecutive days
            if (request.employmentMonths < 3 && request.daysRequested > 2) {
                return false;
            }

            // --- Rule 5: Consecutive-leave limit (HR Policy owns this) ---
            // No more than 15 consecutive days in a calendar year
            if (request.consecutiveDaysThisYear + request.daysRequested > 15) {
                return false;
            }

            // --- Rule 6: Blackout dates (Operations team owns this) ---
            // No leave during product launch / quarter-end
            if (request.isBlackoutDate) {
                return false;
            }

            // --- Rule 7: Role-based limits (Compliance team owns this) ---
            // Contractors cannot take more than 5 days at a time
            if ("CONTRACTOR".equalsIgnoreCase(request.role) && request.daysRequested > 5) {
                return false;
            }

            // --- Rule 8: Medical certificate for >3 days (HR Policy owns this) ---
            if (request.daysRequested > 3 && !request.medicalCertProvided) {
                return false;
            }

            return true;
        }

        // Problem: to explain WHY it was rejected, we have to duplicate all conditions here.
        List<String> getRejectionReasons(LeaveRequest request) {
            // DUPLICATE of canApprove — any change must be made in BOTH places.
            // Three teams will inevitably let these go out of sync.
            List<String> reasons = new java.util.ArrayList<>();
            if (request.leaveBalanceDays < request.daysRequested)
                reasons.add("Insufficient leave balance");
            if (!request.managerApproved)
                reasons.add("Manager has not approved");
            if (request.employmentMonths < 3 && request.daysRequested > 2)
                reasons.add("Probation: max 2 days per request");
            if (request.consecutiveDaysThisYear + request.daysRequested > 15)
                reasons.add("Consecutive-leave limit (15 days/year) exceeded");
            if (request.isBlackoutDate)
                reasons.add("Requested dates fall within a company blackout period");
            if ("CONTRACTOR".equalsIgnoreCase(request.role) && request.daysRequested > 5)
                reasons.add("Contractors may not take more than 5 days at a time");
            if (request.daysRequested > 3 && !request.medicalCertProvided)
                reasons.add("Medical certificate required for requests longer than 3 days");
            return reasons;
        }
    }

    public static void main(String[] args) {
        LeaveApprovalService service = new LeaveApprovalService();

        // Good request
        LeaveRequest good = new LeaveRequest(
            "E001",
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 3),
            true, 10, 12, 5, false, "EMPLOYEE", true
        );
        System.out.println("Good request approved: " + service.canApprove(good));

        // Bad request — multiple failures
        LeaveRequest bad = new LeaveRequest(
            "E002",
            LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 7),
            false, 3, 2, 12, true, "CONTRACTOR", false
        );
        System.out.println("Bad request approved: " + service.canApprove(bad));
        System.out.println("Rejection reasons: " + service.getRejectionReasons(bad));

        System.out.println("\n--- canApprove() is 60 lines. getRejectionReasons() duplicates it.");
        System.out.println("    Three teams edit this file. Logic drift is guaranteed. ---");
    }
}
