package evolution;

/**
 * EVOLUTION v1 — Tightly Coupled IoT Sensor
 *
 * Domain: Temperature Sensor / IoT
 *
 * TemperatureSensor directly calls AlertSystem, Dashboard, and Logger.
 * Adding a 4th consumer requires modifying TemperatureSensor.
 * The sensor knows the internals of 3 unrelated downstream systems.
 *
 * Problems:
 * 1. TemperatureSensor has 3 imports it shouldn't know about.
 * 2. Adding a new consumer = edit TemperatureSensor.
 * 3. Removing a consumer = edit TemperatureSensor.
 * 4. Testing the sensor requires all 3 downstream systems present.
 */
public class v1_TightlyCoupled {

    // ---------------------------------------------------------------
    // Downstream systems — each has its own concern
    // ---------------------------------------------------------------
    static class AlertSystem {
        void trigger(double temperature, String sensorId) {
            if (temperature > 80.0) {
                System.out.println("  [ALERT] HIGH TEMP: sensor=" + sensorId
                        + " temp=" + temperature + "°C — triggering alert!");
            }
        }
    }

    static class Dashboard {
        void refresh(String sensorId, double temperature, long timestamp) {
            System.out.println("  [DASHBOARD] Updating: sensor=" + sensorId
                    + " temp=" + temperature + "°C at t=" + timestamp);
        }
    }

    static class Logger {
        void log(String sensorId, double temperature) {
            System.out.println("  [LOG] sensor=" + sensorId + " temp=" + temperature + "°C");
        }
    }

    // ---------------------------------------------------------------
    // TemperatureSensor — knows about 3 downstream systems
    // ---------------------------------------------------------------
    static class TemperatureSensor {
        private final String sensorId;

        // TIGHT COUPLING: sensor holds direct references to all consumers
        private final AlertSystem alertSystem;
        private final Dashboard   dashboard;
        private final Logger      logger;

        TemperatureSensor(String sensorId,
                          AlertSystem alertSystem,
                          Dashboard dashboard,
                          Logger logger) {
            this.sensorId    = sensorId;
            this.alertSystem = alertSystem;
            this.dashboard   = dashboard;
            this.logger      = logger;
        }

        /**
         * Called by hardware when a new reading arrives.
         * Every downstream call is hard-coded here.
         * Adding AutocoolSystem requires: import + field + new param + call here.
         */
        void onTemperatureRead(double temperature) {
            long now = System.currentTimeMillis();
            System.out.println("[SENSOR:" + sensorId + "] Read: " + temperature + "°C");

            // Coupled call 1
            alertSystem.trigger(temperature, sensorId);

            // Coupled call 2
            dashboard.refresh(sensorId, temperature, now);

            // Coupled call 3
            logger.log(sensorId, temperature);

            // To add AutocoolSystem: add field, add constructor param, add call here.
            // Every new consumer grows this method.
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v1: Tightly Coupled TemperatureSensor ===\n");

        AlertSystem alerts    = new AlertSystem();
        Dashboard   dashboard = new Dashboard();
        Logger      logger    = new Logger();

        TemperatureSensor sensor = new TemperatureSensor("SENSOR-01", alerts, dashboard, logger);

        sensor.onTemperatureRead(22.5);
        System.out.println();
        sensor.onTemperatureRead(85.0);

        System.out.println();
        System.out.println("Problem: TemperatureSensor knows AlertSystem, Dashboard, and Logger.");
        System.out.println("Adding a 4th consumer = edit TemperatureSensor constructor + method.");
        System.out.println("The sensor is responsible for knowing who cares about temperature.");
        System.out.println("That is not the sensor's job.");
    }
}
