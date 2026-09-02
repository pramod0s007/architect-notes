// Overcorrection: tenant isolation IS real here -- this isn't the pressure
// case's mistake, TenantA's flag genuinely doesn't leak to other tenants.
// But it's hardcoded directly into application code as literal string
// comparisons, not stored as data anywhere. This is the version of the
// code currently deployed to production.
class FeatureFlagsV1 {
    static boolean isNewCheckoutEnabled(String tenantId) {
        if (tenantId.equals("TenantA")) {
            return true;
        }
        return false; // every other tenant, including ones that don't exist yet
    }
}
