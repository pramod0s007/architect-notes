public class Main {
    public static void main(String[] args) {
        LocalStore local = new LocalStore();
        boolean firstCreate = local.create("Alice", "user1-data");
        boolean secondCreate = local.create("alice", "user2-data");

        System.out.println("Local dev test: create('Alice') succeeded: " + firstCreate);
        System.out.println("Local dev test: create('alice') succeeded: " + secondCreate);
        System.out.println("Local dev accounts after both creates: " + local.size());

        System.out.println();
        System.out.println(!secondCreate
            ? "FIXED: local dev correctly rejects the duplicate, during development, before the code "
              + "ever reaches production. The developer sees this failure immediately -- in their own "
              + "test run, on their own machine -- and fixes the actual bug (handling the rejection, or "
              + "checking for an existing account first) instead of discovering it after deploy."
            : "Unexpected -- investigate.");
    }
}
