import java.util.*;

// =============================================================================
// BUCKET 4: RULES VARIATION — Report Access Checker
// =============================================================================
// Symptom : if-else chain growing as business rules multiply and combine
// Pressure : RULES variation — rules are owned by compliance, combine with
//            AND/OR, change independently, and compose into policies
// Solution : Specification Pattern
//
// Key insight: Unlike behavior variation (algorithm changes) or object
// variation (collaborator changes), here the RULES themselves are the
// product. They compose with AND/OR, they are owned by a non-engineering
// team, and new rules arrive without touching old ones.
//
// The Specification Pattern names each rule, makes it testable in isolation,
// and lets you combine rules into policies using and()/or()/not() without
// modifying existing specifications.
// =============================================================================

class User {
    private final String id;
    private final boolean admin;
    private final String department;
    private final String tier;           // "standard", "premium", "enterprise"

    public User(String id, boolean admin, String department, String tier) {
        this.id         = id;
        this.admin      = admin;
        this.department = department;
        this.tier       = tier;
    }
    public String getId()         { return id; }
    public boolean isAdmin()      { return admin; }
    public String getDepartment() { return department; }
    public String getTier()       { return tier; }
}

class ReportDoc {
    private final String id;
    private final boolean publicAccess;
    private final String ownerDepartment;
    private final String classification;  // "public", "internal", "confidential", "restricted"
    private final boolean archived;

    public ReportDoc(String id, boolean publicAccess, String ownerDepartment,
                     String classification, boolean archived) {
        this.id               = id;
        this.publicAccess     = publicAccess;
        this.ownerDepartment  = ownerDepartment;
        this.classification   = classification;
        this.archived         = archived;
    }
    public String getId()              { return id; }
    public boolean isPublic()          { return publicAccess; }
    public String getOwnerDepartment() { return ownerDepartment; }
    public String getClassification()  { return classification; }
    public boolean isArchived()        { return archived; }
}

// ---------------------------------------------------------------------------
// v1  (Month 1) — Two rules. CORRECT AS-IS. No pressure yet.
// ---------------------------------------------------------------------------
class ReportAccessCheckerV1 {

    public boolean canAccess(User user, ReportDoc report) {
        if (user.isAdmin())      return true;   // admins see everything
        if (report.isPublic())   return true;   // public reports open to all
        return false;
    }
}

// ---------------------------------------------------------------------------
// v2  (Month 9) — Compliance team added 4 more rules over 8 months.
//
// PAIN POINTS (rules variation signals):
//   [!] Rules combine with AND/OR: "premium OR enterprise AND same dept"
//   [!] Rule ownership is outside engineering — compliance team writes
//       requirements in natural language; engineers translate them
//   [!] Rules change independently: "archived" rule was added month 3,
//       "classification" rule month 6, "department" rule month 8
//   [!] New combination: "restricted reports need same-dept AND premium+"
//   [!] Testing requires constructing a specific User+Report pair for EACH
//       combination of 6 rules — exponential setup cost
//   [!] The if-else is not an algorithm and not a collaborator — it IS the
//       business policy, expressed as code
//
// Diagnosis: Rules variation. Specification Pattern.
// ---------------------------------------------------------------------------
class ReportAccessCheckerV2 {

    public boolean canAccess(User user, ReportDoc report) {
        // Rule 1 (Month 1) — admins bypass all rules
        if (user.isAdmin()) return true;

        // Rule 2 (Month 1) — public reports accessible to anyone
        if (report.isPublic()) return true;

        // Rule 3 (Month 3) — archived reports not accessible to standard users
        if (report.isArchived() && user.getTier().equals("standard")) return false;

        // Rule 4 (Month 6) — confidential reports: same department only
        if (report.getClassification().equals("confidential")) {
            if (!user.getDepartment().equals(report.getOwnerDepartment())) return false;
        }

        // Rule 5 (Month 8) — restricted reports: same department AND premium+
        if (report.getClassification().equals("restricted")) {
            boolean sameDept    = user.getDepartment().equals(report.getOwnerDepartment());
            boolean highTier    = user.getTier().equals("premium") || user.getTier().equals("enterprise");
            if (!(sameDept && highTier)) return false;
        }

        // Rule 6 (Month 9) — enterprise users can access any non-restricted report
        if (user.getTier().equals("enterprise") && !report.getClassification().equals("restricted"))
            return true;

        // [!] Default: standard users with internal reports in same dept get access
        return report.getClassification().equals("internal") &&
               user.getDepartment().equals(report.getOwnerDepartment());
    }
}

// ---------------------------------------------------------------------------
// v3  (Refactored) — Specification Pattern
//
// WHAT CHANGED:
//   - Introduced AccessSpec<T> interface with isSatisfiedBy() and and()/or()/not()
//   - Each business rule is a named Specification class
//   - AccessPolicy composes specs into policies using fluent combinators
//   - canAccess() reads like the compliance team's written requirements
//
// WHY Specification (not Strategy, not Interface+Composition):
//   - The rules COMBINE — and()/or()/not() are first-class combinators
//   - Rules are named (AdminRule, PublicReportRule) — they are self-documenting
//   - Each rule is independently testable with a single User+Report pair
//   - The compliance team can specify new policies in terms of existing specs
//     without touching any existing class
//   - "Add a new rule" = new Spec class + compose into policy. Zero edits.
// ---------------------------------------------------------------------------
interface AccessSpec {
    boolean isSatisfiedBy(User user, ReportDoc report);

    default AccessSpec and(AccessSpec other) {
        return (u, r) -> this.isSatisfiedBy(u, r) && other.isSatisfiedBy(u, r);
    }
    default AccessSpec or(AccessSpec other) {
        return (u, r) -> this.isSatisfiedBy(u, r) || other.isSatisfiedBy(u, r);
    }
    default AccessSpec not() {
        return (u, r) -> !this.isSatisfiedBy(u, r);
    }
}

// Each rule is a named, testable class
class AdminRule implements AccessSpec {
    @Override
    public boolean isSatisfiedBy(User user, ReportDoc report) {
        return user.isAdmin();
    }
}

class PublicReportRule implements AccessSpec {
    @Override
    public boolean isSatisfiedBy(User user, ReportDoc report) {
        return report.isPublic();
    }
}

class NotArchivedOrPremiumPlusRule implements AccessSpec {
    @Override
    public boolean isSatisfiedBy(User user, ReportDoc report) {
        if (!report.isArchived()) return true;
        return user.getTier().equals("premium") || user.getTier().equals("enterprise");
    }
}

class SameDepartmentRule implements AccessSpec {
    @Override
    public boolean isSatisfiedBy(User user, ReportDoc report) {
        return user.getDepartment().equals(report.getOwnerDepartment());
    }
}

class PremiumPlusTierRule implements AccessSpec {
    @Override
    public boolean isSatisfiedBy(User user, ReportDoc report) {
        return user.getTier().equals("premium") || user.getTier().equals("enterprise");
    }
}

class EnterpriseTierRule implements AccessSpec {
    @Override
    public boolean isSatisfiedBy(User user, ReportDoc report) {
        return user.getTier().equals("enterprise");
    }
}

// Policy: reads like the compliance requirement document
class ReportAccessCheckerV3 {

    // Compose rules once — readable, testable, auditable
    private final AccessSpec policy = new AdminRule()
        .or(new PublicReportRule())
        // Restricted: same dept AND premium+
        .or((u, r) -> r.getClassification().equals("restricted") &&
                      new SameDepartmentRule().and(new PremiumPlusTierRule()).isSatisfiedBy(u, r))
        // Confidential: same dept only
        .or((u, r) -> r.getClassification().equals("confidential") &&
                      new SameDepartmentRule().isSatisfiedBy(u, r) &&
                      new NotArchivedOrPremiumPlusRule().isSatisfiedBy(u, r))
        // Enterprise: any non-restricted
        .or((u, r) -> new EnterpriseTierRule().isSatisfiedBy(u, r) &&
                      !r.getClassification().equals("restricted"))
        // Internal: same dept, not standard-archived
        .or((u, r) -> r.getClassification().equals("internal") &&
                      new SameDepartmentRule().isSatisfiedBy(u, r) &&
                      new NotArchivedOrPremiumPlusRule().isSatisfiedBy(u, r));

    // canAccess reads like the policy document, not like imperative code
    public boolean canAccess(User user, ReportDoc report) {
        return policy.isSatisfiedBy(user, report);
    }
}

// ---------------------------------------------------------------------------
// Demo — compiles and runs
// ---------------------------------------------------------------------------
public class bucket4_RulesVariation {
    public static void main(String[] args) {
        User admin         = new User("u1", true,  "finance", "enterprise");
        User premiumFinance = new User("u2", false, "finance", "premium");
        User standardHR    = new User("u3", false, "hr",      "standard");
        User enterpriseMkt = new User("u4", false, "marketing", "enterprise");

        ReportDoc publicReport    = new ReportDoc("r1", true,  "finance", "public",      false);
        ReportDoc confidential    = new ReportDoc("r2", false, "finance", "confidential",false);
        ReportDoc restrictedFin   = new ReportDoc("r3", false, "finance", "restricted",  false);
        ReportDoc archivedInternal = new ReportDoc("r4", false, "hr",     "internal",    true);

        // v1
        System.out.println("=== v1 ===");
        ReportAccessCheckerV1 v1 = new ReportAccessCheckerV1();
        System.out.println("admin    + public  : " + v1.canAccess(admin, publicReport));     // true
        System.out.println("standardHR + conf  : " + v1.canAccess(standardHR, confidential)); // false (v1 has no dept rule)

        // v2
        System.out.println("\n=== v2 ===");
        ReportAccessCheckerV2 v2 = new ReportAccessCheckerV2();
        System.out.println("premiumFin + restr : " + v2.canAccess(premiumFinance, restrictedFin));  // true
        System.out.println("standardHR + arch  : " + v2.canAccess(standardHR, archivedInternal));   // false

        // v3 — Specification Pattern
        System.out.println("\n=== v3 ===");
        ReportAccessCheckerV3 v3 = new ReportAccessCheckerV3();
        System.out.println("admin       + restr  : " + v3.canAccess(admin, restrictedFin));          // true
        System.out.println("premiumFin  + restr  : " + v3.canAccess(premiumFinance, restrictedFin)); // true
        System.out.println("standardHR  + restr  : " + v3.canAccess(standardHR, restrictedFin));     // false
        System.out.println("enterpriseMkt + conf : " + v3.canAccess(enterpriseMkt, confidential));   // false (diff dept)
        System.out.println("enterpriseMkt + public: " + v3.canAccess(enterpriseMkt, publicReport));  // true

        // Testing individual specs in isolation — the Specification benefit
        System.out.println("\n=== Spec unit tests ===");
        AccessSpec sameDept = new SameDepartmentRule();
        System.out.println("sameDept(premiumFin, confidential): "
            + sameDept.isSatisfiedBy(premiumFinance, confidential));  // true
        System.out.println("sameDept(standardHR, confidential): "
            + sameDept.isSatisfiedBy(standardHR, confidential));      // false
    }
}
