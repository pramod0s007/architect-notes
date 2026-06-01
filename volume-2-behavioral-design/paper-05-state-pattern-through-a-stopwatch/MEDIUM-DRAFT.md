# State Pattern Through a StopWatch

*The StopWatch isn't a toy example. It's exactly what every order workflow, payment system, and device firmware eventually becomes.*

---

A few years ago I was brought in to review a subscription management service that had been running in production for about eighteen months.

The service tracked whether a user's subscription was active, and blocked access to premium features accordingly. Simple concept. The original implementation had two states: `ACTIVE` and `CANCELLED`.

By the time I reviewed it, the business had added: `TRIAL`, `TRIAL_EXPIRED`, `PAYMENT_FAILED`, `GRACE_PERIOD`, `SUSPENDED`, `PENDING_CANCELLATION`, and `REACTIVATING`.

Nine states. And the service had grown to match:

```java
void processFeatureRequest(User user) {
    if (user.getStatus().equals("ACTIVE")) {
        // allow
    } else if (user.getStatus().equals("TRIAL")) {
        if (user.getTrialDaysRemaining() > 0) {
            // allow with trial banner
        } else {
            // block with upgrade prompt
        }
    } else if (user.getStatus().equals("GRACE_PERIOD")) {
        if (user.getGracePeriodEndsAt().isAfter(Instant.now())) {
            // allow with payment warning
        } else {
            user.setStatus("SUSPENDED");  // side effect inside feature check
            // block
        }
    }
    // ... 40 more lines
}
```

The `processFeatureRequest` method was 60 lines. The `processPaymentEvent` method was 80 lines. The `processLoginEvent` method was 45 lines. Each had its own version of the same nine state checks. The GRACE_PERIOD → SUSPENDED transition happened in three different places, inconsistently.

When the product team asked to add a `PAUSED` state (users could freeze their subscription for up to 30 days), the engineering estimate was two weeks. For one new state.

**That's state explosion.** Not the number of if-statements — the cost of adding one more mode to a system where state logic is scattered everywhere.

Start with something simple. A stopwatch.

```java
class StopWatch {
    private String state = "IDLE";

    void start() {
        if (state.equals("IDLE") || state.equals("PAUSED")) {
            state = "RUNNING";
        }
    }

    void stop() {
        if (state.equals("RUNNING") || state.equals("PAUSED")) {
            state = "IDLE";
        }
    }

    void pause() {
        if (state.equals("RUNNING")) {
            state = "PAUSED";
        }
    }
}
```

Three states. Three methods. Twelve lines. Readable.

**Don't touch it.** This is correct code. No pattern needed.

---

## The Pressure Arrives

Requirements grow.

Product adds a SUSPENDED state — the watch is frozen mid-run, awaiting an external sync signal. Engineering adds ERROR_RECOVERY — the watch enters a degraded state when the time source is unavailable. QA discovers that calling `start()` while SUSPENDED should behave differently than calling it while PAUSED.

Now the class looks like this:

```java
void start() {
    if (state.equals("IDLE"))          { state = "RUNNING"; initTimer(); }
    if (state.equals("PAUSED"))        { state = "RUNNING"; resumeTimer(); }
    if (state.equals("SUSPENDED"))     { /* wait for sync */ }
    if (state.equals("ERROR_RECOVERY")){ /* log and reject */ }
}

void stop() {
    if (state.equals("RUNNING"))       { state = "IDLE"; saveElapsed(); }
    if (state.equals("PAUSED"))        { state = "IDLE"; discardElapsed(); }
    if (state.equals("SUSPENDED"))     { state = "IDLE"; clearSync(); }
    if (state.equals("ERROR_RECOVERY")){ /* force-reset */ }
}

void pause() {
    if (state.equals("RUNNING"))   { state = "PAUSED"; freezeTimer(); }
    // IDLE, SUSPENDED, ERROR_RECOVERY → do nothing or throw
}

void resume() { ... }
void sync()   { ... }
void reset()  { ... }
```

Six states. Six methods. Thirty-six branches.

And it keeps growing.

Every new state multiplies across every method. Every method that touches state is a potential source of inconsistency. The logic is not in one place — it's scattered.

**This is state explosion.** Not "too many if-statements" — state explosion. The distinction matters because it tells you exactly what to do next.

---

## Why Scattered State Logic Hurts

The real cost isn't the number of lines. It's the invariants.

What happens if `start()` is called when the watch is in ERROR_RECOVERY? Who decides? Right now that logic lives inside `start()`. If the rule changes, you modify `start()` — but the same rule might need to change in `pause()`, `resume()`, `sync()`, and `reset()` too.

Adding a new state means auditing every method for how it should behave in that state.

Removing a state means hunting down every place that checks for it.

The state logic doesn't have a home. It's everywhere.

**State Pattern gives state logic a home.**

---

## The Refactoring

### Step 1: Define what a state looks like

```java
interface WatchState {
    void start(StopWatch context);
    void stop(StopWatch context);
    void pause(StopWatch context);
    void resume(StopWatch context);
}
```

Each state is responsible for its own behavior. If a transition is invalid in a particular state, that state handles the rejection.

### Step 2: Create state implementations

```java
class IdleState implements WatchState {

    public void start(StopWatch context) {
        context.setState(new RunningState());
        context.initTimer();
    }

    public void stop(StopWatch context) {
        // Already idle — no-op
    }

    public void pause(StopWatch context) {
        throw new IllegalStateException("Cannot pause an idle watch");
    }

    public void resume(StopWatch context) {
        throw new IllegalStateException("Cannot resume an idle watch");
    }
}

class RunningState implements WatchState {

    public void start(StopWatch context) {
        // Already running — no-op
    }

    public void stop(StopWatch context) {
        context.setState(new IdleState());
        context.saveElapsed();
    }

    public void pause(StopWatch context) {
        context.setState(new PausedState());
        context.freezeTimer();
    }

    public void resume(StopWatch context) {
        // Already running — no-op
    }
}

class PausedState implements WatchState {

    public void start(StopWatch context) {
        context.setState(new RunningState());
        context.resumeTimer();
    }

    public void stop(StopWatch context) {
        context.setState(new IdleState());
        context.discardElapsed();
    }

    public void pause(StopWatch context) {
        // Already paused — no-op
    }

    public void resume(StopWatch context) {
        context.setState(new RunningState());
        context.resumeTimer();
    }
}
```

### Step 3: The StopWatch becomes a delegator

```java
class StopWatch {

    private WatchState state = new IdleState();

    void setState(WatchState state) { this.state = state; }

    void start()  { state.start(this);  }
    void stop()   { state.stop(this);   }
    void pause()  { state.pause(this);  }
    void resume() { state.resume(this); }
}
```

The StopWatch doesn't know which state is active. It just delegates.

---

## What Changed

**Before:** Six methods, each containing a conditional per state. Adding a new state = editing six methods.

**After:** Each state is a class. Adding a new state = one new class that implements the interface. Existing states are untouched.

**Before:** Invalid transitions handled inconsistently across methods.

**After:** Each state defines exactly which transitions are valid for it. Invalid transitions are handled in one place — the state class.

**Before:** State logic is everywhere.

**After:** State logic has a home.

---

## The Subscription Service — Resolved

Back to the subscription management service from the opening. Nine states. Six operations. Forty-five branches.

With State Pattern:

```java
interface SubscriptionState {
    boolean allowsFeatureAccess(Subscription subscription);
    SubscriptionState onPaymentReceived(Subscription subscription);
    SubscriptionState onPaymentFailed(Subscription subscription);
    SubscriptionState onCancellationRequested(Subscription subscription);
    SubscriptionState onGracePeriodExpired(Subscription subscription);
}

class ActiveState implements SubscriptionState {
    public boolean allowsFeatureAccess(Subscription s) { return true; }

    public SubscriptionState onPaymentFailed(Subscription s) {
        s.setGracePeriodEnd(Instant.now().plus(7, DAYS));
        return new GracePeriodState();
    }

    public SubscriptionState onCancellationRequested(Subscription s) {
        s.setCancelAt(s.getBillingCycleEnd());
        return new PendingCancellationState();
    }

    // other transitions...
}

class GracePeriodState implements SubscriptionState {
    public boolean allowsFeatureAccess(Subscription s) {
        return Instant.now().isBefore(s.getGracePeriodEnd());
    }

    public SubscriptionState onGracePeriodExpired(Subscription s) {
        return new SuspendedState();
    }

    public SubscriptionState onPaymentReceived(Subscription s) {
        return new ActiveState();
    }
}
```

Adding the `PAUSED` state — the one that took two weeks in the original design — became:

1. Create `PausedState` implementing `SubscriptionState`
2. Add `onPauseRequested()` transition in `ActiveState`
3. Add `onResumeRequested()` transition in `PausedState`

Two hours. Not two weeks.

**Each state owns its valid transitions. Adding a state doesn't require touching every method.**

---

## Where This Actually Appears

The StopWatch is the teaching example. The real examples are everywhere:

| Domain | States | What explodes |
|--------|--------|---------------|
| E-commerce order | PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED | Conditional in every order method |
| Payment | INITIATED, AUTHORIZING, AUTHORIZED, CAPTURING, CAPTURED, FAILED, REFUNDING | Processing logic scattered across six handlers |
| Document approval | DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED, ARCHIVED | Workflow transitions in one massive service |
| IoT device | OFFLINE, CONNECTING, SYNCING, ONLINE, ERROR, FIRMWARE_UPDATE | Device commands rejected inconsistently by state |
| User session | ANONYMOUS, AUTHENTICATING, AUTHENTICATED, EXPIRED, LOCKED | Auth checks duplicated across middleware |

Every one of these starts simple and evolves into the StopWatch problem. State Pattern is not overkill for these — it's exactly what they need when transitions multiply.

---

## The Interview Answer

**Question:** What is State Pattern and when should it be used?

**Weak answer:** *"State Pattern lets an object change its behavior based on its internal state."*

**Strong answer:**

*"State Pattern addresses state explosion — when a system's behavior varies based on its current mode, and adding new modes requires editing every method that checks state. The pattern gives state-specific behavior a home: each state becomes a class that owns its valid transitions and responses to operations. Invalid transitions are rejected cleanly by the state, not scattered across conditionals. The right time to introduce it is when a new state forces you to audit multiple methods for how they should behave in that state — that audit cost is the signal."*

---

## Key Takeaways

- State Pattern solves **state explosion**, not if-statement count.
- State-specific behavior belongs in the state, not in every method.
- Invalid transitions are handled by the state — clean, not scattered.
- The signal: adding a new state forces you to edit multiple methods.
- E-commerce orders, payment workflows, and device states all follow this pattern.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 04: Strategy Pattern Through Real Refactoring | Next → Paper 06: Command Pattern Through Banking Systems*
