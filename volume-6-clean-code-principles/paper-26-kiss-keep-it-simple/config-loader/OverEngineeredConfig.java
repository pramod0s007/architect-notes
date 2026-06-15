import java.util.HashMap;
import java.util.Map;

// KISS violation: 4 collaborating classes to do what AppConfig.load() does in 3 lines.
// ConfigProviderRegistry, ConfigTransformer, ConfigValidator, and ConfigMigrationStrategy
// were added speculatively — none of the transformations or migrations exist yet.

interface ConfigTransformer {
    Map<String, String> transform(Map<String, String> raw);
}

interface ConfigMigrationStrategy {
    Map<String, String> migrate(Map<String, String> properties, String fromVersion, String toVersion);
}

class ConfigValidator {
    // KISS violation: validates schema that never changes.
    void validate(Map<String, String> props) {
        if (!props.containsKey("db.url")) throw new RuntimeException("db.url required");
        if (!props.containsKey("server.port")) throw new RuntimeException("server.port required");
    }
}

class ConfigProviderRegistry {
    // KISS violation: a registry of transformers for a single config source.
    private final Map<String, ConfigTransformer> transformers = new HashMap<>();

    public void register(String name, ConfigTransformer transformer) {
        transformers.put(name, transformer);
    }

    public Map<String, String> applyAll(Map<String, String> raw) {
        Map<String, String> result = new HashMap<>(raw);
        for (ConfigTransformer t : transformers.values()) {
            result = t.transform(result);
        }
        return result;
    }
}

/**
 * Over-engineered config loader.
 * Wires four abstractions that add zero value for a 3-property config.
 * The simple AppConfig.load() produces an identical result in 3 lines.
 */
public class OverEngineeredConfig {

    private final ConfigProviderRegistry registry;
    private final ConfigValidator validator;
    private final ConfigMigrationStrategy migrationStrategy;

    public OverEngineeredConfig(ConfigProviderRegistry registry,
                                ConfigValidator validator,
                                ConfigMigrationStrategy migrationStrategy) {
        this.registry = registry;
        this.validator = validator;
        this.migrationStrategy = migrationStrategy;
    }

    public Map<String, String> load(Map<String, String> raw) {
        Map<String, String> migrated = migrationStrategy.migrate(raw, "v1", "v2");
        Map<String, String> transformed = registry.applyAll(migrated);
        validator.validate(transformed);
        return transformed;
    }
}
