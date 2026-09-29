import java.util.Map;

/**
 * The provider team's own automated test suite. It checks the fields the
 * provider itself considers load-bearing -- "id" and "total" -- because
 * those are the fields the provider's own code reads and computes. It has
 * no reason to check "status," because nothing on the provider side reads
 * it back out of its own response; the provider only ever writes it.
 */
public class ProviderOwnTests {
    public static boolean run(Map<String, Object> response) {
        boolean idPresent = response.get("id") != null;
        boolean totalPositive = response.get("total") != null && (double) response.get("total") > 0;
        return idPresent && totalPositive;
    }
}
