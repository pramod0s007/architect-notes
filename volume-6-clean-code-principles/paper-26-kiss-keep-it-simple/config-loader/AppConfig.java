import java.util.Map;

/**
 * KISS: reads three config values from a map (simulating Properties.load).
 * No registry, no transformer pipeline, no migration strategy.
 * Getters only — config is immutable after load.
 */
public class AppConfig {

    private final String dbUrl;
    private final int port;
    private final String logLevel;

    private AppConfig(String dbUrl, int port, String logLevel) {
        this.dbUrl = dbUrl;
        this.port = port;
        this.logLevel = logLevel;
    }

    public static AppConfig load(Map<String, String> properties) {
        String dbUrl    = properties.getOrDefault("db.url", "jdbc:h2:mem:default");
        int port        = Integer.parseInt(properties.getOrDefault("server.port", "8080"));
        String logLevel = properties.getOrDefault("log.level", "INFO");
        return new AppConfig(dbUrl, port, logLevel);
    }

    public String getDbUrl()    { return dbUrl; }
    public int getPort()        { return port; }
    public String getLogLevel() { return logLevel; }

    @Override
    public String toString() {
        return "AppConfig{db=" + dbUrl + ", port=" + port + ", logLevel=" + logLevel + "}";
    }
}
