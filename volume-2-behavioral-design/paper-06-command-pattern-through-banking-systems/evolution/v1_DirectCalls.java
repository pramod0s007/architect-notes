// =============================================================================
// v1 — DIRECT CALLS (Month 1)
// =============================================================================
// Domain: Smart Home Controller
//
// SmartHomeController directly calls device methods.
// 3 devices. Simple, correct, no pattern needed.
//
// Comments: "correct as-is, no pressure yet"
// =============================================================================

public class v1_DirectCalls {

    // ---------------------------------------------------------------------------
    // Device classes — represent physical smart home devices
    // ---------------------------------------------------------------------------
    static class Light {
        private final String room;
        private boolean on = false;

        public Light(String room) { this.room = room; }

        public void turnOn()  { on = true;  System.out.println("[Light:" + room + "] ON");  }
        public void turnOff() { on = false; System.out.println("[Light:" + room + "] OFF"); }
        public boolean isOn() { return on; }
    }

    static class AirConditioner {
        private final String room;
        private int temperature = 24;
        private boolean on = false;

        public AirConditioner(String room) { this.room = room; }

        public void turnOn()             { on = true;  System.out.println("[AC:" + room + "] ON"); }
        public void turnOff()            { on = false; System.out.println("[AC:" + room + "] OFF"); }
        public void setTemperature(int t) {
            this.temperature = t;
            System.out.println("[AC:" + room + "] Temp set to " + t + "°C");
        }
        public int getTemperature()      { return temperature; }
        public boolean isOn()            { return on; }
    }

    static class Fan {
        private final String room;
        private int speed = 0; // 0=off, 1=low, 2=med, 3=high

        public Fan(String room) { this.room = room; }

        public void turnOn(int speed) {
            this.speed = speed;
            System.out.println("[Fan:" + room + "] ON speed=" + speed);
        }
        public void turnOff() { speed = 0; System.out.println("[Fan:" + room + "] OFF"); }
        public int getSpeed() { return speed; }
    }

    // ---------------------------------------------------------------------------
    // Controller — direct method calls. Simple and correct for 3 devices.
    // ---------------------------------------------------------------------------
    static class SmartHomeControllerV1 {
        private final Light    light;
        private final AirConditioner ac;
        private final Fan      fan;

        public SmartHomeControllerV1(Light light, AirConditioner ac, Fan fan) {
            this.light = light;
            this.ac    = ac;
            this.fan   = fan;
        }

        // Bedtime routine: direct calls to each device
        public void bedtimeRoutine() {
            System.out.println("--- Bedtime Routine ---");
            light.turnOff();
            ac.setTemperature(22);
            fan.turnOn(1);
        }

        // Morning routine
        public void morningRoutine() {
            System.out.println("--- Morning Routine ---");
            light.turnOn();
            ac.turnOn();
            ac.setTemperature(24);
            fan.turnOn(2);
        }
    }

    public static void main(String[] args) {
        Light light = new Light("Bedroom");
        AirConditioner ac = new AirConditioner("Bedroom");
        Fan fan = new Fan("Bedroom");

        SmartHomeControllerV1 controller = new SmartHomeControllerV1(light, ac, fan);

        controller.morningRoutine();
        System.out.println();
        controller.bedtimeRoutine();
    }
}
