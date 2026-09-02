import java.util.HashMap;
import java.util.Map;

// Same externalized, environment-specific store as the fix -- no hardcoded
// secrets, this isn't the pressure case's mistake. The overcorrection is in
// rotate(): the new secret REPLACES the old one instantly, with no overlap
// window during which both are still valid.
class SecretStore {
    private final Map<String, String> secretsByEnvironment = new HashMap<>();

    void seed(String environment, String secret) {
        secretsByEnvironment.put(environment, secret);
    }

    String get(String environment) {
        return secretsByEnvironment.get(environment);
    }

    void rotate(String environment, String newSecret) {
        secretsByEnvironment.put(environment, newSecret); // old value is gone, immediately
    }
}
