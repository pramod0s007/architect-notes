package evolution;

/**
 * EVOLUTION v2 — Pressure Mounts: 6 Consumers
 *
 * Domain: Temperature Sensor / IoT
 *
 * The team added 3 new consumers: AutocoolSystem, EnergyMonitor, MaintenanceTracker.
 * Every new consumer required modifying TemperatureSensor:
 *   - New field
 *   - New constructor parameter
 *   - New hard-coded call in onTemperatureRead()
 *
 * TemperatureSensor now knows about 6 unrelated systems.
 * Its constructor has 7 parameters.
 * Removing any consumer also requires editing the sensor.
 * Testing the sensor requires constructing all 6 downstream systems.
 *
 * This is the pressure that motivates the Observer Pattern.
 */
public class v2_MoreConsumers {

    // ---------------------------------------------------------------
    // Original 3 consumers
    // ---------------------------------------------------------------
    static class AlertSystem {
        void trigger(double temp, String id) {
            if (temp > 80.0) System.out.println("  [ALERT] HIGH TEMP: " + id + " = " + temp + "°C");
        }
    }

    static class Dashboard {
        void refresh(String id, double temp, long ts) {
            System.out.println("  [DASHBOARD] " + id + " = " + temp + "°C at t=" + ts);
        }
    }

    static class Logger {
        void log(String id, double temp) {
            System.out.println("  [LOG] " + id + " = " + temp + "°C");
        }
    }

    // ---------------------------------------------------------------
    // Sprint 4: added 3 more consumers
    // ---------------------------------------------------------------
    static class AutocoolSystem {
        void adjust(double temperature) {
            if (temperature > 75.0) {
                System.out.println("  [AUTOCOOL] Increasing cooling, temp=" + temperature + "°C");
            }
        }
    }

    static class EnergyMonitor {
        private double totalEnergy = 0.0;

        void record(double temperature, long durationMs) {
            // Simplified: energy ∝ temperature deviation from 20°C
            double deviation = Math.abs(temperature - 20.0);
            totalEnergy += deviation * (durationMs / 1000.0) * 0.01;
            System.out.println("  [ENERGY] Recorded. Total kWh=" + String.format("%.3f", totalEnergy));
        }
    }

    static class MaintenanceTracker {
        private int highTempCount = 0;

        void checkThreshold(double temperature, String sensorId) {
            if (temperature > 90.0) {
                highTempCount++;
                System.out.println("  [MAINTENANCE] HIGH TEMP count=" + highTempCount
                        + " for sensor=" + sensorId + " — schedule inspection");
            }
        }
    }

    // ---------------------------------------------------------------
    // TemperatureSensor — now knows about 6 downstream systems
    // ---------------------------------------------------------------
    static class TemperatureSensor {
        private final String sensorId;

        // ORIGINAL 3
        private final AlertSystem   alertSystem;
        private final Dashboard     dashboard;
        private final Logger        logger;

        // SPRINT 4: added 3 more — sensor was edited each time
        private final AutocoolSystem    autocool;
        private final EnergyMonitor     energyMonitor;
        private final MaintenanceTracker maintenanceTracker;

        // 7-parameter constructor — grows with every new consumer
        TemperatureSensor(String sensorId,
                          AlertSystem alertSystem,
                          Dashboard dashboard,
                          Logger logger,
                          AutocoolSystem autocool,
                          EnergyMonitor energyMonitor,
                          MaintenanceTracker maintenanceTracker) {
            this.sensorId           = sensorId;
            this.alertSystem        = alertSystem;
            this.dashboard          = dashboard;
            this.logger             = logger;
            this.autocool           = autocool;
            this.energyMonitor      = energyMonitor;
            this.maintenanceTracker = maintenanceTracker;
        }

        /**
         * 6 hard-coded calls. Each was added by a different engineer.
         * Each addition required editing this method.
         * Next sprint: DataAnalytics team wants to subscribe too.
         * That is the 4th edit to this file this quarter.
         */
        void onTemperatureRead(double temperature) {
            long now = System.currentTimeMillis();
            System.out.println("[SENSOR:" + sensorId + "] Read: " + temperature + "°C");

            alertSystem.trigger(temperature, sensorId);                // consumer 1
            dashboard.refresh(sensorId, temperature, now);             // consumer 2
            logger.log(sensorId, temperature);                         // consumer 3
            autocool.adjust(temperature);                              // consumer 4
            energyMonitor.record(temperature, 5000);                   // consumer 5
            maintenanceTracker.checkThreshold(temperature, sensorId);  // consumer 6

            // Consumer 7 coming next sprint: DataAnalytics.ingest(...)
            // That means editing this file AGAIN.
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v2: 6 Consumers — TemperatureSensor Overloaded ===\n");

        TemperatureSensor sensor = new TemperatureSensor(
                "SENSOR-01",
                new AlertSystem(),
                new Dashboard(),
                new Logger(),
                new AutocoolSystem(),
                new EnergyMonitor(),
                new MaintenanceTracker()
        );

        sensor.onTemperatureRead(72.0);
        System.out.println();
        sensor.onTemperatureRead(92.0);

        System.out.println();
        System.out.println("TemperatureSensor now knows 6 downstream systems.");
        System.out.println("Every new consumer = edit sensor constructor + onTemperatureRead().");
        System.out.println("Every removed consumer = edit sensor constructor + onTemperatureRead().");
        System.out.println("The sensor has become the integration hub for the entire IoT platform.");
        System.out.println("Observer Pattern eliminates all 6 tight couplings in v3.");
    }
}
