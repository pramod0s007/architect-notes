import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// Generates a random 3-character base62 code and retries on collision.
// Keyspace is small on purpose (62^3 = 238,328) to make the degradation
// observable in a short-running demo -- the SHAPE of the problem is
// identical at 62^7, just slower to arrive.
class RandomCodeShortener {
    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 3;
    static final long KEYSPACE_SIZE = 238328; // 62^3

    private final Set<String> used = new HashSet<>();
    private final Random random;

    RandomCodeShortener(long seed) {
        this.random = new Random(seed);
    }

    // Returns the number of attempts it took to find a free code.
    int shorten() {
        int attempts = 0;
        while (true) {
            attempts++;
            String candidate = randomCode();
            if (used.add(candidate)) {
                return attempts;
            }
        }
    }

    void prefillTo(double fillFraction) {
        long target = (long) (KEYSPACE_SIZE * fillFraction);
        while (used.size() < target) {
            used.add(randomCode());
        }
    }

    int currentSize() {
        return used.size();
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
