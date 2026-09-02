public class Main {
    public static void main(String[] args) {
        SecretStore store = new SecretStore();
        store.seed("dev", "sk-dev-1a2b3c4d5e6f7a8b");
        store.seed("prod", "sk-live-4f9a2b7c1e6d8f3a");

        System.out.println("Source code contains no literal secret value at all -- only lookups by environment.");
        System.out.println("dev secret:  " + store.get("dev"));
        System.out.println("prod secret: " + store.get("prod"));
        System.out.println("These are two distinct values -- reading one reveals nothing about the other.");

        System.out.println();
        System.out.println("Someone with access to the DEV environment's config leaks the dev secret:");
        String leakedDevSecret = store.get("dev");
        System.out.println("  Leaked: " + leakedDevSecret);

        System.out.println();
        System.out.println("That leaked dev secret is used to try to authorize a real production charge of 500000c:");
        boolean authorized = PaymentGateway.authorizeCharge(store, "prod", leakedDevSecret, 500000);
        System.out.println("  Production charge authorized: " + authorized);

        System.out.println();
        System.out.println(!authorized
            ? "FIXED: the leaked dev secret does not authorize a production charge, because dev and prod "
              + "were never the same value in the first place. Compromising one environment's credential "
              + "compromises exactly that environment -- not every environment the source code happens "
              + "to run in."
            : "Unexpected -- investigate.");
    }
}
