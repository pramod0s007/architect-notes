public class Main {
    // A single, global, shared flag -- there is no tenant dimension in this
    // value at all. Flipping it "for TenantA's rollout" flips it for
    // whoever asks, because the flag has no idea a specific tenant exists.
    static boolean newCheckoutFlowEnabled = false;

    public static void main(String[] args) {
        System.out.println("Before rollout -- TenantA: " + newCheckoutFlowEnabled
            + ", TenantB: " + newCheckoutFlowEnabled + ", TenantC: " + newCheckoutFlowEnabled);

        System.out.println();
        System.out.println("An engineer flips the flag on, intending to enable the new checkout flow");
        System.out.println("for TenantA only, as an early beta:");
        newCheckoutFlowEnabled = true;

        System.out.println();
        System.out.println("After the flip -- TenantA: " + newCheckoutFlowEnabled
            + ", TenantB: " + newCheckoutFlowEnabled + ", TenantC: " + newCheckoutFlowEnabled);

        System.out.println();
        System.out.println(newCheckoutFlowEnabled
            ? "CONFIRMED FAILURE: TenantB and TenantC -- who were never part of the beta and never asked "
              + "for the new checkout flow -- are now seeing it too, identically to TenantA. The flag "
              + "has no tenant dimension at all, so there was never a way to change it for one tenant "
              + "without changing it for every tenant reading the same shared value."
            : "Unexpected -- investigate.");
    }
}
