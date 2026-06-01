import java.util.*;

// =============================================================================
// v2 — GROWING PROBLEM (Month 1 → Month 8)
// =============================================================================
// Domain: Smart Home Controller
//
// Three requirements arrived that direct calls cannot satisfy:
//
// REQUIREMENT 1 (Month 3): UNDO
//   "When I press undo on the remote, the last action should be reversed."
//   Direct calls have no history. Once light.turnOn() is called, there is no
//   way to know "we need to turn it off" without storing extra state.
//
// REQUIREMENT 2 (Month 5): SCHEDULING
//   "Turn off all lights at 11pm automatically."
//   Scheduled execution means the controller.turnOffLight() call must be
//   deferred — stored as a unit of work and executed later. Direct calls
//   execute immediately.
//
// REQUIREMENT 3 (Month 6): MACRO COMMANDS
//   "Goodnight macro: lights off + AC 22°C + fan speed 1."
//   A macro is a sequence of actions that should execute atomically and be
//   undoable as a unit. Direct calls give no composability.
//
// REQUIREMENT 4 (Month 8): 8 DEVICES
//   5 new devices added (TV, Thermostat, Door Lock, Speaker, Blinds).
//   The controller class has grown to manage 8 devices × N methods each.
//   It is now a 300+ line monolith with no clear seam.
//
// PAIN POINTS labeled with [!] below.
// =============================================================================

public class v2_GrowingProblem {

    // Devices (same as v1 + 5 new ones)
    static class Light {
        private final String room; private boolean on;
        public Light(String room) { this.room = room; }
        public void turnOn()  { on = true;  System.out.println("[Light:" + room + "] ON"); }
        public void turnOff() { on = false; System.out.println("[Light:" + room + "] OFF"); }
        public boolean isOn() { return on; }
    }

    static class AirConditioner {
        private final String room; private int temperature = 24; private boolean on;
        public AirConditioner(String room) { this.room = room; }
        public void turnOn()             { on = true;  System.out.println("[AC:" + room + "] ON"); }
        public void turnOff()            { on = false; System.out.println("[AC:" + room + "] OFF"); }
        public void setTemperature(int t) { temperature = t; System.out.println("[AC:" + room + "] " + t + "°C"); }
        public int getTemperature()       { return temperature; }
        public boolean isOn()             { return on; }
    }

    static class Fan {
        private final String room; private int speed;
        public Fan(String room) { this.room = room; }
        public void setSpeed(int s) { speed = s; System.out.println("[Fan:" + room + "] speed=" + s); }
        public int getSpeed()       { return speed; }
    }

    static class Television {
        private final String room; private boolean on; private int channel = 1;
        public Television(String room) { this.room = room; }
        public void turnOn()           { on = true;  System.out.println("[TV:" + room + "] ON"); }
        public void turnOff()          { on = false; System.out.println("[TV:" + room + "] OFF"); }
        public void setChannel(int c)  { channel = c; System.out.println("[TV:" + room + "] ch=" + c); }
        public boolean isOn()          { return on; }
    }

    static class DoorLock {
        private final String name; private boolean locked = false;
        public DoorLock(String name) { this.name = name; }
        public void lock()   { locked = true;  System.out.println("[Lock:" + name + "] LOCKED"); }
        public void unlock() { locked = false; System.out.println("[Lock:" + name + "] UNLOCKED"); }
        public boolean isLocked() { return locked; }
    }

    static class Speaker {
        private final String room; private int volume = 50; private boolean on;
        public Speaker(String room) { this.room = room; }
        public void turnOn()         { on = true;  System.out.println("[Speaker:" + room + "] ON"); }
        public void turnOff()        { on = false; System.out.println("[Speaker:" + room + "] OFF"); }
        public void setVolume(int v) { volume = v; System.out.println("[Speaker:" + room + "] vol=" + v); }
        public int getVolume()       { return volume; }
    }

    static class Blinds {
        private final String room; private int position = 100; // 0=closed, 100=fully open
        public Blinds(String room) { this.room = room; }
        public void setPosition(int p) { position = p; System.out.println("[Blinds:" + room + "] pos=" + p + "%"); }
        public int getPosition()       { return position; }
    }

    // ---------------------------------------------------------------------------
    // The controller that has grown into a problem
    //
    // [!] 8 devices, all wired directly. Any new device = change constructor.
    // [!] Undo implemented as a stack of strings + a big if-else to reverse —
    //     adding a new undoable action = add another else-if to the undo method
    // [!] Scheduling is simulated — in real code this would be a tangled mess
    //     of Timer + direct calls with no abstraction
    // [!] Macro commands are hardcoded methods — goodnight(), movie(), etc.
    //     Each macro knows about ALL devices and must be updated when devices change
    // ---------------------------------------------------------------------------
    static class SmartHomeControllerV2 {

        // [!] Constructor takes 7 devices. Grows with every new device.
        private final Light light1, light2;
        private final AirConditioner ac;
        private final Fan fan;
        private final Television tv;
        private final DoorLock door;
        private final Speaker speaker;
        private final Blinds blinds;

        // [!] Undo history as strings — a hack. Every new action needs a string constant.
        private final Deque<String> undoHistory = new ArrayDeque<>();
        private int lastLightBrightness = -1;
        private int lastAcTemp = -1;
        private int lastFanSpeed = -1;
        private int lastVolume = -1;
        private int lastBlindsPos = -1;

        public SmartHomeControllerV2(Light l1, Light l2, AirConditioner ac, Fan fan,
                                     Television tv, DoorLock door, Speaker speaker, Blinds blinds) {
            this.light1   = l1; this.light2 = l2; this.ac = ac; this.fan = fan;
            this.tv       = tv; this.door = door; this.speaker = speaker; this.blinds = blinds;
        }

        // Individual device actions — each saves undo info separately
        public void turnOnLight1() {
            undoHistory.push("light1_off");  // [!] manual undo bookkeeping
            light1.turnOn();
        }
        public void turnOffLight1() {
            undoHistory.push("light1_on");
            light1.turnOff();
        }
        public void setAcTemp(int temp) {
            lastAcTemp = ac.getTemperature();
            undoHistory.push("ac_temp_" + lastAcTemp); // [!] save previous value as string
            ac.setTemperature(temp);
        }
        public void setFanSpeed(int speed) {
            lastFanSpeed = fan.getSpeed();
            undoHistory.push("fan_speed_" + lastFanSpeed);
            fan.setSpeed(speed);
        }
        public void lockDoor() {
            undoHistory.push("door_unlock");
            door.lock();
        }
        public void setVolume(int vol) {
            lastVolume = speaker.getVolume();
            undoHistory.push("speaker_vol_" + lastVolume);
            speaker.setVolume(vol);
        }
        public void setBlinds(int pos) {
            lastBlindsPos = blinds.getPosition();
            undoHistory.push("blinds_" + lastBlindsPos);
            blinds.setPosition(pos);
        }

        // [!] Undo: a large if-else parsing string tokens — fragile and hard to extend
        public void undo() {
            if (undoHistory.isEmpty()) {
                System.out.println("[UNDO] Nothing to undo.");
                return;
            }
            String last = undoHistory.pop();
            System.out.println("[UNDO] Reversing: " + last);
            if (last.equals("light1_off"))       light1.turnOff();
            else if (last.equals("light1_on"))   light1.turnOn();
            else if (last.startsWith("ac_temp_")) ac.setTemperature(Integer.parseInt(last.split("_")[2]));
            else if (last.startsWith("fan_speed_")) fan.setSpeed(Integer.parseInt(last.split("_")[2]));
            else if (last.equals("door_unlock")) door.unlock();
            else if (last.startsWith("speaker_vol_")) speaker.setVolume(Integer.parseInt(last.split("_")[2]));
            else if (last.startsWith("blinds_"))  blinds.setPosition(Integer.parseInt(last.split("_")[1]));
            else System.out.println("[UNDO] Unknown undo token: " + last);
        }

        // [!] Macro commands: hardcoded, know about all 8 devices, not undoable as a unit
        public void goodnightMacro() {
            System.out.println("--- GOODNIGHT MACRO ---");
            turnOffLight1(); turnOffLight1(); // both lights
            setAcTemp(22);
            setFanSpeed(1);
            lockDoor();
            speaker.turnOff();
            blinds.setPosition(0);
        }

        public void movieMacro() {
            System.out.println("--- MOVIE MACRO ---");
            light1.turnOff();
            tv.turnOn();
            tv.setChannel(4);
            speaker.turnOn();
            setVolume(70);
            blinds.setPosition(0);
        }

        // [!] Scheduling: not really implemented — would need Timer + direct call
        // This cannot be abstracted without making commands first-class objects
        public void scheduleOff(String deviceName, int delaySeconds) {
            System.out.println("[SCHEDULE] Would turn off " + deviceName + " in " + delaySeconds + "s (not implemented)");
        }
    }

    public static void main(String[] args) {
        Light l1 = new Light("Living Room"); Light l2 = new Light("Bedroom");
        AirConditioner ac = new AirConditioner("Bedroom");
        Fan fan = new Fan("Bedroom"); Television tv = new Television("Living Room");
        DoorLock door = new DoorLock("Front"); Speaker speaker = new Speaker("Living Room");
        Blinds blinds = new Blinds("Living Room");

        SmartHomeControllerV2 ctrl = new SmartHomeControllerV2(l1, l2, ac, fan, tv, door, speaker, blinds);

        System.out.println("=== Individual actions with undo ===");
        ctrl.turnOnLight1();
        ctrl.setAcTemp(18);
        ctrl.setFanSpeed(3);
        ctrl.undo();  // undoes fan
        ctrl.undo();  // undoes AC
        ctrl.undo();  // undoes light

        System.out.println("\n=== Goodnight macro ===");
        ctrl.goodnightMacro();

        System.out.println("\n=== Movie macro ===");
        ctrl.movieMacro();
        ctrl.undo(); // [!] only undoes the last individual action, not the whole macro

        // [!] Cannot undo an entire macro as one unit
        // [!] Cannot schedule a macro to run at 11pm cleanly
        // [!] Adding device #9 means changing the constructor + every macro method
    }
}
