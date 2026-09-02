public class Main {
    public static void main(String[] args) {
        // Alice and Bob independently chose the same password -- a completely
        // ordinary, common occurrence at any real scale of users.
        String alicePassword = "Summer2024!";
        String bobPassword = "Summer2024!";

        String aliceStoredHash = PasswordHasher.hash(alicePassword);
        String bobStoredHash = PasswordHasher.hash(bobPassword);

        System.out.println("Alice's stored password hash: " + aliceStoredHash);
        System.out.println("Bob's stored password hash:   " + bobStoredHash);

        boolean identical = aliceStoredHash.equals(bobStoredHash);
        System.out.println();
        System.out.println(identical
            ? "CONFIRMED FAILURE: Alice's and Bob's stored hashes are byte-for-byte identical. Anyone "
              + "with read access to this table -- an attacker, a curious insider, a breached backup -- "
              + "can tell these two accounts share the same password just by comparing two hash values, "
              + "without cracking either one. And because SHA-256 is a fast, general-purpose hash, an "
              + "attacker can also test enormous dictionaries of common passwords against every stored "
              + "hash in the table in a short time, since no salt forces each guess to be repeated "
              + "per-account, and no deliberate slowness raises the cost of any individual guess."
            : "Unexpected -- investigate.");
    }
}
