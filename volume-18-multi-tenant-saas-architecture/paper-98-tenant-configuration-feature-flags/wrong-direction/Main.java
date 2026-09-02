public class Main {
    public static void main(String[] args) {
        System.out.println("Currently deployed code (V1): TenantA enabled, hardcoded in source.");
        System.out.println("A new tenant, TenantD, signs up today and also needs the new checkout flow:");
        System.out.println("  TenantA: " + FeatureFlagsV1.isNewCheckoutEnabled("TenantA"));
        System.out.println("  TenantD: " + FeatureFlagsV1.isNewCheckoutEnabled("TenantD"));

        System.out.println();
        System.out.println("The only way to turn it on for TenantD is to write new source code and ship");
        System.out.println("a new deploy (V2) with TenantD added to the hardcoded check -- there is no");
        System.out.println("config value anywhere that could have granted this without a code change:");
        System.out.println("  TenantA: " + FeatureFlagsV2.isNewCheckoutEnabled("TenantA"));
        System.out.println("  TenantD: " + FeatureFlagsV2.isNewCheckoutEnabled("TenantD"));

        boolean deployRequired = !FeatureFlagsV1.isNewCheckoutEnabled("TenantD")
            && FeatureFlagsV2.isNewCheckoutEnabled("TenantD");
        System.out.println();
        System.out.println(deployRequired
            ? "CONFIRMED FAILURE: enabling one more tenant required a source code change and a full "
              + "deploy cycle (V1 to V2), not a data update. Tenant isolation is real, but every single "
              + "new tenant-specific override -- onboarding a customer, running a beta, rolling back a "
              + "feature for one account -- is coupled to the release pipeline. At ten tenants that's an "
              + "inconvenience; at ten thousand it's an engineering team doing nothing but merging "
              + "one-line tenant-ID changes."
            : "Unexpected -- investigate.");
    }
}
