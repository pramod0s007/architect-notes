# Paper 06 — Command Pattern: Evolution Examples

Domain: **Smart Home Controller** — a domain where four practical requirements (undo, scheduling, macros, new devices) all become solvable once actions are first-class objects.

## The Trigger

Direct calls work fine for 3 devices with no undo. The pattern becomes necessary when **all four** of these requirements arrive:

1. **Undo** — "when I press undo on the remote, reverse the last action"
2. **Scheduling** — "turn off all lights at 11pm automatically"
3. **Macro commands** — "goodnight = lights off + AC 22°C + fan speed 1, undoable as a unit"
4. **New devices** — "we added 5 more devices"

None of these can be satisfied cleanly with direct calls.

---

## Files

| File | Stage | Problem |
|------|-------|---------|
| `v1_DirectCalls.java` | Month 1 | 3 devices, direct calls, correct as-is |
| `v2_GrowingProblem.java` | Month 8 | 8 devices, undo as string tokens, hardcoded macros |
| `v3_CommandApplied.java` | Refactored | ICommand + CommandInvoker + MacroCommand composite |

---

## Progression

**v1 (Month 1)** — `light.turnOn()`, `ac.setTemperature(24)`, `fan.turnOn(2)`. Three devices. Correct and minimal.

**v2 (Month 8)** — Five devices added. Undo implemented as a stack of string tokens parsed by a giant if-else. Macros hardcoded as methods that reference all 8 devices. Scheduling not really solvable. Pain points marked `[!]`.

**v3 (Refactored)** — `ICommand` with `execute()` and `undo()`. Each command stores its own previous state (e.g. `AcTemperatureCommand` captures `previousTemp` at execute time). `CommandInvoker` maintains the undo stack. `MacroCommand` is a Composite — `undo()` reverses all sub-commands in reverse order.

---

## What the Pattern Unlocks

| Requirement | v2 approach | v3 approach |
|---|---|---|
| Undo single action | Parse string token, if-else | `command.undo()` — no if-else |
| Undo macro as unit | Impossible — individual undos only | `MacroCommand.undo()` reverses all |
| Scheduling | Timer + direct call (tangled) | Store `ICommand`, execute at trigger time |
| New device | Change constructor + every macro method | New command class only |

---

## Structure

```
ICommand
  ├── execute()
  └── undo()

Concrete commands:
  LightOnCommand, LightOffCommand
  AcTemperatureCommand
  FanSpeedCommand
  DoorLockCommand
  SpeakerVolumeCommand
  BlindsCommand
  MacroCommand (Composite — holds List<ICommand>)

CommandInvoker
  ├── execute(ICommand)   → calls execute(), pushes to history
  ├── undo()              → pops history, calls undo()
  └── scheduleFor(ICommand, time)

SmartHomeController
  └── Creates commands, hands to invoker — zero direct device calls
```

---

## How to Run

```bash
javac v1_DirectCalls.java
javac v2_GrowingProblem.java
javac v3_CommandApplied.java

java v1_DirectCalls
java v2_GrowingProblem
java v3_CommandApplied
```

No external dependencies. All files are self-contained.
