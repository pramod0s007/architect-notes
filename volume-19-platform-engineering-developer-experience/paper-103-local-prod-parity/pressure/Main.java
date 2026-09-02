public class Main {
    public static void main(String[] args) {
        // A developer tests locally against LocalStore only -- that's the
        // only environment they ever actually run their code against.
        LocalStore local = new LocalStore();
        boolean firstCreateLocal = local.create("Alice", "user1-data");
        boolean secondCreateLocal = local.create("alice", "user2-data");

        System.out.println("Local dev test: create('Alice') succeeded: " + firstCreateLocal);
        System.out.println("Local dev test: create('alice') succeeded: " + secondCreateLocal);
        System.out.println("Local dev accounts after both creates: " + local.size());

        // The identical sequence of operations, against a store that
        // faithfully matches production's real case-insensitive collation --
        // representing what would actually happen on deploy.
        ProdLikeStore prod = new ProdLikeStore();
        boolean firstCreateProd = prod.create("Alice", "user1-data");
        boolean secondCreateProd = prod.create("alice", "user2-data");

        System.out.println();
        System.out.println("Same code, production's real collation: create('Alice') succeeded: " + firstCreateProd);
        System.out.println("Same code, production's real collation: create('alice') succeeded: " + secondCreateProd);
        System.out.println("Production accounts after both creates: " + prod.size());

        System.out.println();
        System.out.println(secondCreateLocal && !secondCreateProd
            ? "CONFIRMED FAILURE: the exact same code, given the exact same inputs, succeeded locally "
              + "and would have been rejected in production. Local dev's case-sensitive comparison and "
              + "production's real case-insensitive collation disagree about whether 'Alice' and "
              + "'alice' are the same account -- and the only environment a developer actually tests "
              + "against never surfaces that disagreement."
            : "Unexpected -- investigate.");
    }
}
