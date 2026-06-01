/**
 * v4 — TrafficLight: State Pressure Evolution
 *
 * A second example showing a different pressure type: State Explosion.
 * Progression from integer flags → named constants → enum → State Pattern.
 *
 * Inspired by the trainer's StopWatch progression (p1/).
 * Domain changed: TrafficLight (simpler domain, universally understood).
 */

// ════════════════════════════════════════════════════════════════
// STAGE A: Integer flags — works, but meaningless numbers
// ════════════════════════════════════════════════════════════════

class TrafficLightV1 {
    int flag = 0; // 0=red, 1=green, 2=yellow — magic numbers

    public void nextState() {
        if (flag == 0) flag = 1;       // red → green
        else if (flag == 1) flag = 2;  // green → yellow
        else if (flag == 2) flag = 0;  // yellow → red
    }

    public boolean canVehiclePass() {
        return flag == 1; // what is 1 again?
    }
}

// ════════════════════════════════════════════════════════════════
// STAGE B: Named constants — better, still all in one class
// ════════════════════════════════════════════════════════════════

class TrafficLightV2 {
    static final int RED = 0, GREEN = 1, YELLOW = 2;
    int state = RED;

    public void nextState() {
        if (state == RED)    state = GREEN;
        else if (state == GREEN)  state = YELLOW;
        else if (state == YELLOW) state = RED;
    }

    public boolean canVehiclePass() { return state == GREEN; }
    public boolean mustStop()       { return state == RED;   }
}

// ════════════════════════════════════════════════════════════════
// STAGE C: Enum — type-safe, no magic numbers
// ════════════════════════════════════════════════════════════════

class TrafficLightV3 {

    enum Signal { RED, GREEN, YELLOW }

    Signal current = Signal.RED;

    public void nextState() {
        switch (current) {
            case RED:    current = Signal.GREEN;  break;
            case GREEN:  current = Signal.YELLOW; break;
            case YELLOW: current = Signal.RED;    break;
        }
    }

    public boolean canVehiclePass() { return current == Signal.GREEN; }
}

// ════════════════════════════════════════════════════════════════
// STAGE D: State Pattern — when states grow and transitions multiply
//
// Imagine adding: FLASHING_RED (emergency), FLASHING_YELLOW (caution),
// PEDESTRIAN_CROSSING, OUT_OF_SERVICE.
// Every new state + every new behavior = O(n*m) conditional branches.
// State Pattern gives each state a home.
// ════════════════════════════════════════════════════════════════

interface LightState {
    LightState next();              // what state follows this one
    boolean vehiclesMayPass();      // behavior specific to this state
    boolean pedestriansMayCross();
    String displayColor();
}

class RedLight implements LightState {
    public LightState next()              { return new GreenLight(); }
    public boolean vehiclesMayPass()      { return false; }
    public boolean pedestriansMayCross()  { return true;  }
    public String displayColor()          { return "RED"; }
}

class GreenLight implements LightState {
    public LightState next()              { return new YellowLight(); }
    public boolean vehiclesMayPass()      { return true;  }
    public boolean pedestriansMayCross()  { return false; }
    public String displayColor()          { return "GREEN"; }
}

class YellowLight implements LightState {
    public LightState next()              { return new RedLight(); }
    public boolean vehiclesMayPass()      { return false; } // slow down
    public boolean pedestriansMayCross()  { return false; }
    public String displayColor()          { return "YELLOW"; }
}

// Adding FLASHING_RED for emergencies: one new class, no existing state changes.
class FlashingRedLight implements LightState {
    public LightState next()              { return new RedLight(); } // returns to normal
    public boolean vehiclesMayPass()      { return false; }
    public boolean pedestriansMayCross()  { return false; }
    public String displayColor()          { return "FLASHING RED"; }
}

class TrafficLightV4 {

    private LightState currentState = new RedLight();

    public void advance() {
        currentState = currentState.next();
    }

    public void emergencyOverride() {
        currentState = new FlashingRedLight();
    }

    public void display() {
        System.out.printf("Signal: %-15s | Vehicles: %-5s | Pedestrians: %s%n",
            currentState.displayColor(),
            currentState.vehiclesMayPass() ? "GO" : "STOP",
            currentState.pedestriansMayCross() ? "WALK" : "WAIT");
    }
}

class TrafficLightDemo {
    public static void main(String[] args) {
        TrafficLightV4 light = new TrafficLightV4();

        // Normal cycle
        for (int i = 0; i < 4; i++) {
            light.display();
            light.advance();
        }

        System.out.println("--- Emergency Override ---");
        light.emergencyOverride();
        light.display();
    }
}

// ---------------------------------------------------------------
// PRESSURE IDENTIFIED: State Explosion
//   - Adding a new state (FlashingRed) in V2 requires editing
//     nextState(), canVehiclePass(), mustStop() — all methods
//   - In V4 (State Pattern): one new class, zero existing state changes
//
// LESSON: State Pattern did not emerge from the number of if-statements.
//         It emerged from the cost of adding a new state.
// ---------------------------------------------------------------
