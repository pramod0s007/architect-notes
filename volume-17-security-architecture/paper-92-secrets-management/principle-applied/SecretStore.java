import java.util.HashMap;
import java.util.Map;

// Stands in for a real secrets manager (Vault, AWS Secrets Manager, etc.):
// secrets live OUTSIDE the source code entirely, one distinct value per
// environment. The source code never contains a literal secret value --
// only a lookup by environment name.
class SecretStore {
    private final Map<String, String> secretsByEnvironment = new HashMap<>();

    void seed(String environment, String secret) {
        secretsByEnvironment.put(environment, secret);
    }

    String get(String environment) {
        return secretsByEnvironment.get(environment);
    }
}
