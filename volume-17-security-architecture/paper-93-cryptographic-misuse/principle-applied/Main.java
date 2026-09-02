public class Main {
    public static void main(String[] args) {
        String alicePassword = "Summer2024!";
        String bobPassword = "Summer2024!";
        // Each user gets a unique salt, generated once at account creation and
        // stored alongside the hash. Hardcoded here for reproducible output --
        // a real system generates these with a cryptographically secure RNG.
        String aliceSalt = "8f3a1c9e2b7d4f6a";
        String bobSalt = "2c9d5e8f1a4b7c3d";

        long start1 = System.nanoTime();
        String aliceStoredHash = PasswordHasher.hash(alicePassword, aliceSalt);
        long aliceHashNanos = System.nanoTime() - start1;

        String bobStoredHash = PasswordHasher.hash(bobPassword, bobSalt);

        System.out.println("Alice's stored hash: " + aliceStoredHash);
        System.out.println("Bob's stored hash:   " + bobStoredHash);

        boolean identical = aliceStoredHash.equals(bobStoredHash);
        System.out.println();
        System.out.println("Hashes identical: " + identical);
        System.out.println("Time to compute one hash at " + PasswordHasher.ITERATIONS
            + " iterations on this run: ~" + (aliceHashNanos / 1_000_000) + "ms "
            + "(a real per-guess cost an attacker now pays for every single password attempted)");

        System.out.println();
        System.out.println(!identical
            ? "FIXED: Alice's and Bob's stored hashes are completely different, even though they chose "
              + "the identical password, because each account has its own unique salt. Nobody with "
              + "read access to this table can tell these two accounts share a password anymore -- and "
              + "every guess an attacker makes now costs " + PasswordHasher.ITERATIONS + " hash "
              + "computations instead of one, raising the real cost of brute-forcing this table by the "
              + "same factor."
            : "Unexpected -- investigate.");
    }
}
