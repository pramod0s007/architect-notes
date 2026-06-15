/**
 * KISS: reads config, starts up. Readable in seconds.
 */
public class Application {

    private final AppConfig config;

    public Application(AppConfig config) {
        this.config = config;
    }

    public void start() {
        System.out.println("Starting application...");
        System.out.println("  DB   : " + config.getDbUrl());
        System.out.println("  Port : " + config.getPort());
        System.out.println("  Log  : " + config.getLogLevel());
        System.out.println("Application ready on port " + config.getPort());
    }
}
