// v3_SpecificationApplied.java
// Specification pattern: each rule is its own class — independently testable.
// Composing with .and() / .or() makes the policy readable.
// getRejectionReasons() falls out for free — no duplication.

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class v3_SpecificationApplied {

    // ─── Domain model ─────────────────────────────────────────────────────────

    static class LeaveRequest {
        final String    employeeId;
        final LocalDate startDate;
        final LocalDate endDate;
        final int       daysRequested;
        final boolean   managerApproved;
        final int       leaveBalanceDays;
        final int       employmentMonths;
        final int       consecutiveDaysThisYear;
        final boolean   isBlackoutDate;
        final String    role;
        final boolean   medicalCertProvided;

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

    // ─── Specification interface ──────────────────────────────────────────────

    interface Specification<T> {
        boolean isSatisfiedBy(T candidate);
        String  rejectionReason();   // free: same logic, different return type

        default Specification<T> and(Specification<T> other) {
            return new AndSpecification<>(this, other);
        }

        default Specification<T> or(Specification<T> other) {
            return new OrSpecification<>(this, other);
        }
    }

    static class AndSpecification<T> implements Specification<T> {
        private final Specification<T> left;
        private final Specification<T> right;
        AndSpecification(Specification<T> left, Specification<T> right) {
            this.left = left; this.right = right;
        }
        @Override public boolean isSatisfiedBy(T c) {
            return left.isSatisfiedBy(c) && right.isSatisfiedBy(c);
        }
        @Override public String rejectionReason() {
            return left.rejectionReason() + " AND " + right.rejectionReason();
        }
    }

    static class OrSpecification<T> implements Specification<T> {
        private final Specification<T> left;
        private final Specification<T> right;
        OrSpecification(Specification<T> left, Specification<T> right) {
            this.left = left; this.right = right;
        }
        @Override public boolean isSatisfiedBy(T c) {
            return left.isSatisfiedBy(c) || right.isSatisfiedBy(c);
        }
        @Override public String rejectionReason() {
            return left.rejectionReason() + " OR " + right.rejectionReason();
        }
    }

    // ─── Concrete specifications (each owned by one team) ────────────────────

    // Payroll team
    static class LeaveBalanceSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) {
            return r.leaveBalanceDays >= r.daysRequested;
        }
        @Override public String rejectionReason() {
            return "Insufficient leave balance";
        }
    }

    // HR Ops team
    static class NoOverlapSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) {
            return true; // simplified: in production, query approved-leave table
        }
        @Override public String rejectionReason() {
            return "Overlaps with existing approved leave";
        }
    }

    // Line Manager
    static class ManagerApprovedSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) { return r.managerApproved; }
        @Override public String rejectionReason()     { return "Manager has not approved the request"; }
    }

    // HR Policy team
    static class ProbationSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) {
            return !(r.employmentMonths < 3 && r.daysRequested > 2);
        }
        @Override public String rejectionReason() {
            return "Employees on probation (< 3 months) may not take more than 2 consecutive days";
        }
    }

    // HR Policy team
    static class ConsecutiveLimitSpec implements Specification<LeaveRequest> {
        private final int maxConsecutiveDaysPerYear;
        ConsecutiveLimitSpec(int max) { this.maxConsecutiveDaysPerYear = max; }
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) {
            return r.consecutiveDaysThisYear + r.daysRequested <= maxConsecutiveDaysPerYear;
        }
        @Override public String rejectionReason() {
            return "Consecutive-leave limit (" + maxConsecutiveDaysPerYear + " days/year) would be exceeded";
        }
    }

    // Operations team
    static class NotBlackoutDateSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) { return !r.isBlackoutDate; }
        @Override public String rejectionReason()     { return "Requested dates fall within a company blackout period"; }
    }

    // Compliance team
    static class RoleBasedLimitSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) {
            return !("CONTRACTOR".equalsIgnoreCase(r.role) && r.daysRequested > 5);
        }
        @Override public String rejectionReason() {
            return "Contractors may not take more than 5 consecutive days";
        }
    }

    // HR Policy team
    static class MedicalCertSpec implements Specification<LeaveRequest> {
        @Override
        public boolean isSatisfiedBy(LeaveRequest r) {
            return r.daysRequested <= 3 || r.medicalCertProvided;
        }
        @Override public String rejectionReason() {
            return "Medical certificate required for requests longer than 3 days";
        }
    }

    // ─── Service — composes specs, exposes two operations from one source ─────

    static class LeaveApprovalService {

        // All specs in one ordered list — single source of truth
        private final List<Specification<LeaveRequest>> eligibilitySpecs;

        LeaveApprovalService() {
            eligibilitySpecs = new ArrayList<>();
            eligibilitySpecs.add(new LeaveBalanceSpec());
            eligibilitySpecs.add(new NoOverlapSpec());
            eligibilitySpecs.add(new ManagerApprovedSpec());
            eligibilitySpecs.add(new ProbationSpec());
            eligibilitySpecs.add(new ConsecutiveLimitSpec(15));
            eligibilitySpecs.add(new NotBlackoutDateSpec());
            eligibilitySpecs.add(new RoleBasedLimitSpec());
            eligibilitySpecs.add(new MedicalCertSpec());
        }

        boolean canApprove(LeaveRequest request) {
            return getRejectionReasons(request).isEmpty();
        }

        // Free: same loop, collect reasons instead of short-circuiting
        List<String> getRejectionReasons(LeaveRequest request) {
            List<String> reasons = new ArrayList<>();
            for (Specification<LeaveRequest> spec : eligibilitySpecs) {
                if (!spec.isSatisfiedBy(request)) {
                    reasons.add(spec.rejectionReason());
                }
            }
            return reasons;
        }
    }

    // ─── Demo ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        LeaveApprovalService service = new LeaveApprovalService();

        LeaveRequest good = new LeaveRequest(
            "E001",
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 3),
            true, 10, 12, 5, false, "EMPLOYEE", true
        );

        System.out.println("=== Good request ===");
        System.out.println("Approved: " + service.canApprove(good));
        System.out.println("Reasons: "  + service.getRejectionReasons(good));

        LeaveRequest bad = new LeaveRequest(
            "E002",
            LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 7),
            false, 3, 2, 12, true, "CONTRACTOR", false
        );

        System.out.println("\n=== Bad request (multiple failures) ===");
        System.out.println("Approved: " + service.canApprove(bad));
        System.out.println("Reasons:");
        service.getRejectionReasons(bad).forEach(r -> System.out.println("  - " + r));

        // Demo: ad-hoc composed spec without touching the service
        System.out.println("\n=== Ad-hoc: is this request eligible ignoring manager approval? ===");
        Specification<LeaveRequest> relaxed = new LeaveBalanceSpec()
            .and(new NotBlackoutDateSpec())
            .and(new MedicalCertSpec());
        System.out.println("Relaxed check on bad request: " + relaxed.isSatisfiedBy(bad));

        System.out.println("\nKey insight: adding a new rule = one new Specification class.");
        System.out.println("getRejectionReasons() is NOT a copy — it shares the spec list.");
    }
}
