package evolution;

import java.util.ArrayList;
import java.util.List;

/**
 * EVOLUTION v3 — Observer Pattern Applied
 *
 * Domain: Temperature Sensor / IoT
 *
 * TemperatureSensor knows nothing about AlertObserver, DashboardObserver,
 * LogObserver, or AutocoolObserver. It only knows the TemperatureObserver
 * interface. Any number of observers can subscribe or unsubscribe at runtime
 * without touching the sensor.
 *
 * Adding a new consumer (e.g., DataAnalyticsObserver):
 *   1. Implement TemperatureObserver
 *   2. Call sensor.subscribe(new DataAnalyticsObserver())
 *   Done. Zero changes to TemperatureSensor.
 */
public class v3_ObserverApplied {

    // ---------------------------------------------------------------
    // Event — carries the data consumers need
    // ---------------------------------------------------------------
    record TemperatureEvent(String sensorId, double temperature, long timestamp) {}

    // ---------------------------------------------------------------
    // Observer interface — all consumers implement this
    // ---------------------------------------------------------------
    interface TemperatureObserver {
        void onTemperatureChanged(TemperatureEvent event);
    }

    // ---------------------------------------------------------------
    // Subject — TemperatureSensor knows ONLY the interface
    // ---------------------------------------------------------------
    static class TemperatureSensor {
        private final String sensorId;
        private final List<TemperatureObserver> observers = new ArrayList<>();

        TemperatureSensor(String sensorId) {
            this.sensorId = sensorId;
        }

        /** Add a consumer — sensor does not know its type. */
        public void subscribe(TemperatureObserver observer) {
            observers.add(observer);
        }

        /** Remove a consumer — no method signature changes here. */
        public void unsubscribe(TemperatureObserver observer) {
            observers.remove(observer);
        }

        /**
         * Called by hardware when a reading arrives.
         * Sensor publishes the event. Who handles it is not its concern.
         */
        public void onTemperatureRead(double temperature) {
            TemperatureEvent event = new TemperatureEvent(
                    sensorId, temperature, System.currentTimeMillis());

            System.out.println("[SENSOR:" + sensorId + "] Read: " + temperature + "°C — notifying "
                    + observers.size() + " observers");

            for (TemperatureObserver observer : observers) {
                observer.onTemperatureChanged(event);
            }
        }
    }

    // ---------------------------------------------------------------
    // Observers — each handles one concern, sensor knows none of them
    // ---------------------------------------------------------------

    static class AlertObserver implements TemperatureObserver {
        private final double criticalThreshold;

        AlertObserver(double criticalThreshold) {
            this.criticalThreshold = criticalThreshold;
        }

        @Override
        public void onTemperatureChanged(TemperatureEvent event) {
            if (event.temperature() > criticalThreshold) {
                System.out.println("  [ALERT] HIGH TEMP: sensor=" + event.sensorId()
                        + " temp=" + event.temperature() + "°C > threshold=" + criticalThreshold);
            }
        }
    }

    static class DashboardObserver implements TemperatureObserver {
        @Override
        public void onTemperatureChanged(TemperatureEvent event) {
            System.out.println("  [DASHBOARD] Updating: sensor=" + event.sensorId()
                    + " temp=" + event.temperature() + "°C at t=" + event.timestamp());
        }
    }

    static class LogObserver implements TemperatureObserver {
        @Override
        public void onTemperatureChanged(TemperatureEvent event) {
            System.out.println("  [LOG] sensor=" + event.sensorId()
                    + " temp=" + event.temperature() + "°C");
        }
    }

    static class AutocoolObserver implements TemperatureObserver {
        private final double activationThreshold;

        AutocoolObserver(double activationThreshold) {
            this.activationThreshold = activationThreshold;
        }

        @Override
        public void onTemperatureChanged(TemperatureEvent event) {
            if (event.temperature() > activationThreshold) {
                System.out.println("  [AUTOCOOL] Activated for sensor=" + event.sensorId()
                        + " temp=" + event.temperature() + "°C");
            }
        }
    }

    /**
     * New consumer added this sprint — DataAnalyticsObserver.
     * TemperatureSensor was NOT modified. Zero other files changed.
     */
    static class DataAnalyticsObserver implements TemperatureObserver {
        private double sum = 0.0;
        private int    count = 0;

        @Override
        public void onTemperatureChanged(TemperatureEvent event) {
            sum   += event.temperature();
            count += 1;
            double avg = sum / count;
            System.out.printf("  [ANALYTICS] sensor=%s running avg=%.1f°C (n=%d)%n",
                    event.sensorId(), avg, count);
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v3: Observer Pattern Applied ===\n");

        TemperatureSensor sensor = new TemperatureSensor("SENSOR-01");

        // Subscribe observers — sensor knows nothing about their types
        sensor.subscribe(new AlertObserver(80.0));
        sensor.subscribe(new DashboardObserver());
        sensor.subscribe(new LogObserver());
        sensor.subscribe(new AutocoolObserver(75.0));
        sensor.subscribe(new DataAnalyticsObserver()); // sprint 5 addition, sensor unchanged

        System.out.println("--- Normal temperature ---");
        sensor.onTemperatureRead(22.5);

        System.out.println();
        System.out.println("--- High temperature ---");
        sensor.onTemperatureRead(85.0);

        System.out.println();
        System.out.println("--- Dynamic unsubscribe: remove Dashboard ---");
        TemperatureObserver dashboard = new DashboardObserver();
        sensor.subscribe(dashboard);    // add a second dashboard first
        sensor.unsubscribe(dashboard);  // then immediately remove it
        System.out.println("Dashboard unsubscribed. Remaining observers: 5");
        sensor.onTemperatureRead(70.0);

        System.out.println();
        System.out.println("=== Comparison ===");
        System.out.println("v1: sensor knew 3 systems directly.");
        System.out.println("v2: sensor knew 6 systems directly. Grew every sprint.");
        System.out.println("v3: sensor knows ZERO downstream systems. Observer interface only.");
        System.out.println("Adding a 7th consumer: implement TemperatureObserver + subscribe(). Done.");
    }
}
