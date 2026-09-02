public class Main {
    // Overcorrection: salting is added -- this isn't the pressure case's
    // mistake, and iteration count is identical to the fix. But the salt is
    // ONE hardcoded constant, shared by every single account in the system,
    // instead of a unique value generated per credential.
    static final String GLOBAL_SALT = "app-wide-fixed-salt-v1";

    public static void main(String[] args) {
        String alicePassword = "Summer2024!";
        String bobPassword = "Summer2024!";

        String aliceStoredHash = PasswordHasher.hash(alicePassword, GLOBAL_SALT);
        String bobStoredHash = PasswordHasher.hash(bobPassword, GLOBAL_SALT);

        System.out.println("Alice's stored hash: " + aliceStoredHash);
        System.out.println("Bob's stored hash:   " + bobStoredHash);

        boolean identical = aliceStoredHash.equals(bobStoredHash);
        System.out.println();
        System.out.println("Hashes identical: " + identical);

        System.out.println();
        System.out.println(identical
            ? "CONFIRMED FAILURE: Alice's and Bob's stored hashes are byte-for-byte identical again, "
              + "despite salting and iteration both being present. A salt only prevents an attacker from "
              + "telling two accounts apart by their hash if the salt is unique PER ACCOUNT -- a single "
              + "constant shared by everyone contributes the identical bytes to every hash computation, "
              + "so it changes nothing about whether two identical passwords produce identical hashes. "
              + "The iteration count still raises the cost of a single guess, but the specific failure "
              + "salting exists to prevent -- identical passwords producing identical, comparable hashes -- "
              + "is completely unfixed."
            : "Unexpected -- investigate.");
    }
}
