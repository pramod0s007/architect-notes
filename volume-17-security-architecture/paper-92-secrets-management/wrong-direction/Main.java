public class Main {
    public static void main(String[] args) {
        SecretStore store = new SecretStore();
        store.seed("prod", "sk-live-4f9a2b7c1e6d8f3a");

        // A long-running batch job fetches the prod secret ONCE, at the
        // start of a multi-step process, exactly as any legitimate caller
        // with a cached credential would.
        String batchJobsCachedSecret = store.get("prod");
        System.out.println("Batch job starts, fetches the current prod secret once: " + batchJobsCachedSecret);

        System.out.println();
        System.out.println("Midway through the batch job's run, prod secret is rotated (scheduled, routine):");
        store.rotate("prod", "sk-live-9c3f1a8e2b7d4c6a");
        System.out.println("  New prod secret is now: " + store.get("prod"));

        System.out.println();
        System.out.println("The batch job, still using the secret it fetched at start-up, tries to authorize");
        System.out.println("a real, legitimate charge of 250000c:");
        boolean authorized = PaymentGateway.authorizeCharge(store, "prod", batchJobsCachedSecret, 250000);
        System.out.println("  Charge authorized: " + authorized);

        System.out.println();
        System.out.println(!authorized
            ? "CONFIRMED FAILURE: the batch job's request is completely legitimate -- it holds a real "
              + "secret that was genuinely valid at the moment it was issued, from a caller that did "
              + "nothing wrong. Rotation replaced the old secret the instant the new one was created, "
              + "with no window in which both remained valid, so every in-flight caller holding the "
              + "previous secret fails, indistinguishable from an attacker using a stolen one."
            : "Unexpected -- investigate.");
    }
}
