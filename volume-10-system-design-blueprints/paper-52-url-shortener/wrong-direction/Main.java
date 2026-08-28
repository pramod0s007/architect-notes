import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final int ID_SPACE = 1_048_576; // must match NoCheckRandomIdShortener
    private static final int REQUESTS = 2000;

    public static void main(String[] args) {
        NoCheckRandomIdShortener shortener = new NoCheckRandomIdShortener(7);

        // The shortener itself never checks -- this is us, after the fact,
        // discovering what it silently handed out to two different URLs.
        Map<String, Integer> issuedCount = new HashMap<>();
        int collisions = 0;
        for (int i = 0; i < REQUESTS; i++) {
            String code = shortener.shorten();
            int seenBefore = issuedCount.getOrDefault(code, 0);
            if (seenBefore > 0) {
                collisions++;
            }
            issuedCount.put(code, seenBefore + 1);
        }

        double expectedFirstCollisionAt = Math.sqrt(Math.PI * ID_SPACE / 2.0);

        System.out.println("ID space=" + ID_SPACE + " (\"basically infinite\" for " + REQUESTS + " requests)");
        System.out.println("Requests issued=" + REQUESTS + ", collisions actually handed out=" + collisions);
        System.out.printf("Birthday-paradox math predicts a collision by ~%.0f requests, not %,d.%n", expectedFirstCollisionAt, ID_SPACE);
        System.out.println("Two different URLs silently got the SAME short code -- one of them is now broken.");
    }
}
