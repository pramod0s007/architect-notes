import java.util.*;

// =============================================================================
// v3 — COMMAND PATTERN APPLIED
// =============================================================================
// Domain: Smart Home Controller
//
// WHAT CHANGED from v2:
//   - ICommand interface: execute() + undo()
//   - Concrete commands: LightOnCommand, LightOffCommand, AcTemperatureCommand,
//     FanSpeedCommand, DoorLockCommand, SpeakerVolumeCommand, BlindsCommand
//   - CommandInvoker: executes commands, maintains undo stack
//   - MacroCommand (Composite): groups multiple commands, undoable as a unit
//   - SmartHomeController now knows only about invoker + devices — zero direct calls
//
// WHY Command Pattern:
//   - UNDO: each command stores the previous state. undo() reverses itself.
//     No string tokens, no giant if-else in the controller.
//   - SCHEDULING: commands are objects — store them in a queue, execute later.
//     A scheduler just calls command.execute() at the right time.
//   - MACROS (Composite): MacroCommand holds a list of ICommand — execute()
//     runs all of them; undo() reverses all of them in reverse order.
//   - NEW DEVICES: add a new command class. The controller, invoker, and all
//     existing commands are unchanged.
//
// The key insight: making an ACTION a first-class OBJECT unlocks all four
// requirements simultaneously.
// =============================================================================

// ---------------------------------------------------------------------------
// Devices (unchanged from v1/v2)
// ---------------------------------------------------------------------------
class SmartLight {
    private final String room; private boolean on;
    public SmartLight(String room) { this.room = room; }
    public void turnOn()  { on = true;  System.out.println("[Light:" + room + "] ON"); }
    public void turnOff() { on = false; System.out.println("[Light:" + room + "] OFF"); }
    public boolean isOn() { return on; }
}

class SmartAC {
    private final String room; private int temperature = 24; private boolean on;
    public SmartAC(String room) { this.room = room; }
    public void turnOn()              { on = true;  System.out.println("[AC:" + room + "] ON"); }
    public void turnOff()             { on = false; System.out.println("[AC:" + room + "] OFF"); }
    public void setTemperature(int t) { temperature = t; System.out.println("[AC:" + room + "] " + t + "°C"); }
    public int getTemperature()       { return temperature; }
    public boolean isOn()             { return on; }
}

class SmartFan {
    private final String room; private int speed;
    public SmartFan(String room) { this.room = room; }
    public void setSpeed(int s) { speed = s; System.out.println("[Fan:" + room + "] speed=" + s); }
    public int getSpeed()       { return speed; }
}

class SmartDoorLock {
    private final String name; private boolean locked;
    public SmartDoorLock(String name) { this.name = name; }
    public void lock()   { locked = true;  System.out.println("[Lock:" + name + "] LOCKED"); }
    public void unlock() { locked = false; System.out.println("[Lock:" + name + "] UNLOCKED"); }
    public boolean isLocked() { return locked; }
}

class SmartSpeaker {
    private final String room; private int volume = 50; private boolean on;
    public SmartSpeaker(String room) { this.room = room; }
    public void turnOn()          { on = true;  System.out.println("[Speaker:" + room + "] ON"); }
    public void turnOff()         { on = false; System.out.println("[Speaker:" + room + "] OFF"); }
    public void setVolume(int v)  { volume = v; System.out.println("[Speaker:" + room + "] vol=" + v); }
    public int getVolume()        { return volume; }
}

class SmartBlinds {
    private final String room; private int position = 100;
    public SmartBlinds(String room) { this.room = room; }
    public void setPosition(int p) { position = p; System.out.println("[Blinds:" + room + "] " + p + "%"); }
    public int getPosition()       { return position; }
}

// ---------------------------------------------------------------------------
// ICommand — the core abstraction
// Making an action a first-class object is the entire point of this pattern
// ---------------------------------------------------------------------------
interface ICommand {
    void execute();
    void undo();
    String description();
}

// ---------------------------------------------------------------------------
// Concrete Commands — each stores enough state to reverse itself
// ---------------------------------------------------------------------------

class LightOnCommand implements ICommand {
    private final SmartLight light;
    public LightOnCommand(SmartLight light) { this.light = light; }
    @Override public void execute() { light.turnOn(); }
    @Override public void undo()    { light.turnOff(); }  // reverse: turn off
    @Override public String description() { return "LightOn"; }
}

class LightOffCommand implements ICommand {
    private final SmartLight light;
    public LightOffCommand(SmartLight light) { this.light = light; }
    @Override public void execute() { light.turnOff(); }
    @Override public void undo()    { light.turnOn(); }   // reverse: turn on
    @Override public String description() { return "LightOff"; }
}

class AcTemperatureCommand implements ICommand {
    private final SmartAC ac;
    private final int targetTemp;
    private int previousTemp;  // captured at execute() time

    public AcTemperatureCommand(SmartAC ac, int targetTemp) {
        this.ac = ac; this.targetTemp = targetTemp;
    }
    @Override public void execute() {
        previousTemp = ac.getTemperature();  // save previous
        ac.setTemperature(targetTemp);
    }
    @Override public void undo() { ac.setTemperature(previousTemp); }  // restore
    @Override public String description() { return "AcTemp(" + targetTemp + "°C)"; }
}

class FanSpeedCommand implements ICommand {
    private final SmartFan fan;
    private final int targetSpeed;
    private int previousSpeed;

    public FanSpeedCommand(SmartFan fan, int targetSpeed) {
        this.fan = fan; this.targetSpeed = targetSpeed;
    }
    @Override public void execute() {
        previousSpeed = fan.getSpeed();
        fan.setSpeed(targetSpeed);
    }
    @Override public void undo()    { fan.setSpeed(previousSpeed); }
    @Override public String description() { return "FanSpeed(" + targetSpeed + ")"; }
}

class DoorLockCommand implements ICommand {
    private final SmartDoorLock door;
    public DoorLockCommand(SmartDoorLock door) { this.door = door; }
    @Override public void execute() { door.lock(); }
    @Override public void undo()    { door.unlock(); }
    @Override public String description() { return "DoorLock"; }
}

class SpeakerVolumeCommand implements ICommand {
    private final SmartSpeaker speaker;
    private final int targetVolume;
    private int previousVolume;

    public SpeakerVolumeCommand(SmartSpeaker speaker, int targetVolume) {
        this.speaker = speaker; this.targetVolume = targetVolume;
    }
    @Override public void execute() {
        previousVolume = speaker.getVolume();
        speaker.setVolume(targetVolume);
    }
    @Override public void undo()    { speaker.setVolume(previousVolume); }
    @Override public String description() { return "SpeakerVol(" + targetVolume + ")"; }
}

class BlindsCommand implements ICommand {
    private final SmartBlinds blinds;
    private final int targetPosition;
    private int previousPosition;

    public BlindsCommand(SmartBlinds blinds, int targetPosition) {
        this.blinds = blinds; this.targetPosition = targetPosition;
    }
    @Override public void execute() {
        previousPosition = blinds.getPosition();
        blinds.setPosition(targetPosition);
    }
    @Override public void undo()    { blinds.setPosition(previousPosition); }
    @Override public String description() { return "Blinds(" + targetPosition + "%)"; }
}

// ---------------------------------------------------------------------------
// MacroCommand (Composite Pattern + Command Pattern)
// Executes N commands as one unit. Undoes them as one unit in reverse order.
// This is the "goodnight" / "movie" / "good morning" scenario.
// ---------------------------------------------------------------------------
class MacroCommand implements ICommand {
    private final String macroName;
    private final List<ICommand> commands;

    public MacroCommand(String macroName, ICommand... commands) {
        this.macroName = macroName;
        this.commands  = Arrays.asList(commands);
    }

    @Override
    public void execute() {
        System.out.println("--- MACRO:" + macroName + " START ---");
        for (ICommand cmd : commands) cmd.execute();
        System.out.println("--- MACRO:" + macroName + " DONE ---");
    }

    @Override
    public void undo() {
        System.out.println("--- UNDO MACRO:" + macroName + " ---");
        // Reverse order — last executed, first undone
        List<ICommand> reversed = new ArrayList<>(commands);
        Collections.reverse(reversed);
        for (ICommand cmd : reversed) cmd.undo();
    }

    @Override
    public String description() { return "Macro(" + macroName + ")"; }
}

// ---------------------------------------------------------------------------
// CommandInvoker — executes commands and maintains undo stack
// The invoker knows NOTHING about devices — it only knows ICommand
// ---------------------------------------------------------------------------
class CommandInvoker {
    private final Deque<ICommand> history = new ArrayDeque<>();

    // Execute and remember for undo
    public void execute(ICommand command) {
        command.execute();
        history.push(command);
    }

    // Undo the last command
    public void undo() {
        if (history.isEmpty()) {
            System.out.println("[UNDO] Nothing to undo.");
            return;
        }
        ICommand last = history.pop();
        System.out.println("[UNDO] Reversing: " + last.description());
        last.undo();
    }

    // Undo multiple commands
    public void undoN(int n) {
        for (int i = 0; i < n && !history.isEmpty(); i++) undo();
    }

    // Scheduling: store a command to be executed "later"
    // In real code: command would go into a scheduled queue
    public void scheduleFor(ICommand command, String when) {
        System.out.println("[SCHEDULED] " + command.description() + " at " + when);
        // Simulated — a real scheduler would call execute(command) at the right time
    }
}

// ---------------------------------------------------------------------------
// SmartHomeController — thin coordinator. Knows devices, creates commands,
// hands them to the invoker. Zero direct device calls after commands are created.
// ---------------------------------------------------------------------------
class SmartHomeController {
    private final CommandInvoker invoker;

    // Devices
    private final SmartLight   light1, light2;
    private final SmartAC      ac;
    private final SmartFan     fan;
    private final SmartDoorLock door;
    private final SmartSpeaker speaker;
    private final SmartBlinds  blinds;

    public SmartHomeController(CommandInvoker invoker,
                               SmartLight l1, SmartLight l2, SmartAC ac, SmartFan fan,
                               SmartDoorLock door, SmartSpeaker speaker, SmartBlinds blinds) {
        this.invoker = invoker;
        this.light1 = l1; this.light2 = l2; this.ac = ac; this.fan = fan;
        this.door = door; this.speaker = speaker; this.blinds = blinds;
    }

    // Individual actions — create command, hand to invoker
    public void turnOnLight(SmartLight light) { invoker.execute(new LightOnCommand(light)); }
    public void turnOffLight(SmartLight light) { invoker.execute(new LightOffCommand(light)); }
    public void setAcTemp(int temp)           { invoker.execute(new AcTemperatureCommand(ac, temp)); }
    public void setFanSpeed(int speed)        { invoker.execute(new FanSpeedCommand(fan, speed)); }
    public void lockDoor()                    { invoker.execute(new DoorLockCommand(door)); }
    public void setVolume(int vol)            { invoker.execute(new SpeakerVolumeCommand(speaker, vol)); }
    public void setBlinds(int pos)            { invoker.execute(new BlindsCommand(blinds, pos)); }
    public void undo()                        { invoker.undo(); }
    public void undoN(int n)                  { invoker.undoN(n); }

    // Macro commands — composed from existing commands, undoable as one unit
    public void goodnightMacro() {
        invoker.execute(new MacroCommand("Goodnight",
            new LightOffCommand(light1),
            new LightOffCommand(light2),
            new AcTemperatureCommand(ac, 22),
            new FanSpeedCommand(fan, 1),
            new DoorLockCommand(door),
            new BlindsCommand(blinds, 0)
        ));
    }

    public void movieMacro() {
        invoker.execute(new MacroCommand("Movie",
            new LightOffCommand(light1),
            new SpeakerVolumeCommand(speaker, 70),
            new BlindsCommand(blinds, 0)
        ));
    }

    public void morningMacro() {
        invoker.execute(new MacroCommand("Morning",
            new LightOnCommand(light1),
            new LightOnCommand(light2),
            new AcTemperatureCommand(ac, 24),
            new FanSpeedCommand(fan, 2),
            new BlindsCommand(blinds, 100)
        ));
    }

    // Scheduling — command is a first-class object, can be handed to any scheduler
    public void scheduleGoodnight(String time) {
        ICommand goodnight = new MacroCommand("Goodnight-Scheduled",
            new LightOffCommand(light1), new LightOffCommand(light2),
            new AcTemperatureCommand(ac, 22), new FanSpeedCommand(fan, 1)
        );
        invoker.scheduleFor(goodnight, time);
    }
}

// ---------------------------------------------------------------------------
// Demo
// ---------------------------------------------------------------------------
public class v3_CommandApplied {
    public static void main(String[] args) {
        SmartLight   l1  = new SmartLight("Living Room");
        SmartLight   l2  = new SmartLight("Bedroom");
        SmartAC      ac  = new SmartAC("Bedroom");
        SmartFan     fan = new SmartFan("Bedroom");
        SmartDoorLock door = new SmartDoorLock("Front");
        SmartSpeaker spk = new SmartSpeaker("Living Room");
        SmartBlinds  bld = new SmartBlinds("Living Room");

        CommandInvoker invoker = new CommandInvoker();
        SmartHomeController ctrl = new SmartHomeController(invoker, l1, l2, ac, fan, door, spk, bld);

        // Individual actions with undo
        System.out.println("=== Individual actions ===");
        ctrl.turnOnLight(l1);
        ctrl.setAcTemp(18);
        ctrl.setFanSpeed(3);

        System.out.println("\n=== Undo last 2 actions ===");
        ctrl.undoN(2);

        // Macro command
        System.out.println("\n=== Goodnight macro ===");
        ctrl.goodnightMacro();

        // Undo the ENTIRE macro as one unit
        System.out.println("\n=== Undo goodnight macro (entire macro reversed) ===");
        ctrl.undo();

        // Movie macro
        System.out.println("\n=== Movie macro ===");
        ctrl.movieMacro();
        ctrl.undo();  // undo entire movie macro

        // Morning macro
        System.out.println("\n=== Morning macro ===");
        ctrl.morningMacro();

        // Scheduling
        System.out.println("\n=== Schedule goodnight for 11pm ===");
        ctrl.scheduleGoodnight("23:00");

        // Adding a new device (Thermostat) — zero changes to existing code
        System.out.println("\n=== New device (Thermostat) — no existing changes ===");
        // New device class
        class Thermostat {
            int temp = 20;
            void setTemp(int t) { temp = t; System.out.println("[Thermostat] " + t + "°C"); }
            int getTemp()       { return temp; }
        }
        Thermostat thermostat = new Thermostat();

        // New command class — that's all
        ICommand thermostatCmd = new ICommand() {
            final int prev = thermostat.getTemp();
            @Override public void execute()          { thermostat.setTemp(21); }
            @Override public void undo()             { thermostat.setTemp(prev); }
            @Override public String description()    { return "ThermostatSet(21)"; }
        };

        invoker.execute(thermostatCmd);
        invoker.undo();  // undo thermostat — no changes to SmartHomeController
    }
}
