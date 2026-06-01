/**
 * Immutable database configuration object built via the Builder pattern.
 * Handles the complexity of many optional fields, sane defaults, and
 * validation rules that span multiple fields.
 */
public final class DatabaseConfig {

    // Required
    private final String url;
    private final String username;
    private final String password;

    // Optional with defaults
    private final int poolSize;
    private final boolean sslEnabled;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final int maxRetries;
    private final String readReplicaUrl;

    private DatabaseConfig(Builder builder) {
        this.url              = builder.url;
        this.username         = builder.username;
        this.password         = builder.password;
        this.poolSize         = builder.poolSize;
        this.sslEnabled       = builder.sslEnabled;
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.readTimeoutMs    = builder.readTimeoutMs;
        this.maxRetries       = builder.maxRetries;
        this.readReplicaUrl   = builder.readReplicaUrl;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getUrl()               { return url; }
    public String getUsername()          { return username; }
    public String getPassword()          { return password; }
    public int    getPoolSize()          { return poolSize; }
    public boolean isSslEnabled()        { return sslEnabled; }
    public long   getConnectTimeoutMs()  { return connectTimeoutMs; }
    public long   getReadTimeoutMs()     { return readTimeoutMs; }
    public int    getMaxRetries()        { return maxRetries; }
    public String getReadReplicaUrl()    { return readReplicaUrl; }
    public boolean hasReadReplica()      { return readReplicaUrl != null && !readReplicaUrl.isBlank(); }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
               "url='" + url + '\'' +
               ", username='" + username + '\'' +
               ", poolSize=" + poolSize +
               ", sslEnabled=" + sslEnabled +
               ", connectTimeoutMs=" + connectTimeoutMs +
               ", readTimeoutMs=" + readTimeoutMs +
               ", maxRetries=" + maxRetries +
               ", readReplicaUrl='" + (readReplicaUrl != null ? readReplicaUrl : "none") + '\'' +
               '}';
    }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static class Builder {

        // Required — no defaults
        private final String url;
        private final String username;
        private final String password;

        // Optional — sensible defaults
        private int    poolSize         = 10;
        private boolean sslEnabled      = false;
        private long   connectTimeoutMs = 5_000L;
        private long   readTimeoutMs    = 30_000L;
        private int    maxRetries       = 3;
        private String readReplicaUrl   = null;

        public Builder(String url, String username, String password) {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("Database URL is required");
            }
            this.url      = url;
            this.username = username;
            this.password = password;
        }

        public Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder sslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }

        public Builder connectTimeoutMs(long connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder readTimeoutMs(long readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder readReplicaUrl(String readReplicaUrl) {
            this.readReplicaUrl = readReplicaUrl;
            return this;
        }

        public DatabaseConfig build() {
            validate();
            return new DatabaseConfig(this);
        }

        private void validate() {
            if (poolSize < 1 || poolSize > 100) {
                throw new IllegalStateException(
                    "poolSize must be between 1 and 100, got: " + poolSize);
            }
            if (connectTimeoutMs <= 0) {
                throw new IllegalStateException(
                    "connectTimeoutMs must be positive, got: " + connectTimeoutMs);
            }
            if (readTimeoutMs <= 0) {
                throw new IllegalStateException(
                    "readTimeoutMs must be positive, got: " + readTimeoutMs);
            }
            if (maxRetries < 0) {
                throw new IllegalStateException(
                    "maxRetries cannot be negative, got: " + maxRetries);
            }
        }
    }
}
