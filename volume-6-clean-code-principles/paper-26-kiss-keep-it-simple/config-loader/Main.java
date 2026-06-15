import java.util.HashMap;
import java.util.Map;

/**
 * KISS — config-loader
 *
 * Simple AppConfig.load() handles startup in one readable line.
 * The over-engineered alternative requires wiring 4 classes to reach
 * the same result with no additional capability.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Simple config loader ===");
        Map<String, String> properties = new HashMap<>();
        properties.put("db.url",      "jdbc:postgresql://localhost:5432/shopdb");
        properties.put("server.port", "9090");
        properties.put("log.level",   "DEBUG");

        AppConfig config = AppConfig.load(properties);
        new Application(config).start();

        System.out.println();
        System.out.println("=== Same result with the over-engineered alternative ===");
        ConfigProviderRegistry registry = new ConfigProviderRegistry();
        registry.register("identity", raw -> raw);         // no-op transformer
        ConfigValidator validator = new ConfigValidator();
        ConfigMigrationStrategy noOpMigration = (props, from, to) -> props;

        OverEngineeredConfig overEngineered = new OverEngineeredConfig(registry, validator, noOpMigration);
        Map<String, String> result = overEngineered.load(properties);
        System.out.println("Loaded via 4-class pipeline: " + result.get("db.url"));
        System.out.println("AppConfig.load() needed 1 method call. Both produce identical output.");
    }
}
