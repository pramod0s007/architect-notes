// A NEW, separately compiled and separately deployed version of the code --
// the only way TenantD's business team could get the flag turned on was for
// an engineer to edit this method's source and ship a new release.
class FeatureFlagsV2 {
    static boolean isNewCheckoutEnabled(String tenantId) {
        if (tenantId.equals("TenantA") || tenantId.equals("TenantD")) {
            return true;
        }
        return false;
    }
}
