# Paper 137: Horizontal Pod Autoscaling & Scaling Lag

Part of *Architect Notes* Volume 25 — Cloud-Native & Kubernetes Architecture.

## Scenario

Horizontal Pod Autoscaling reacts to a metric crossing a threshold by changing pod count — but the gap between "the autoscaler decides to add a pod" and "that pod is actually serving traffic" is real, unavoidable time. If demand outruns that gap during a sharp spike, a fully correct, well-configured autoscaler can still fail requests, because correct detection and available capacity are two different moments.

## The Three Versions

### `pressure/`
A naive autoscaler evaluates every 2 ticks and adds exactly 1 pod when overloaded, but each pod takes 3 ticks to become useful. Demand jumps 5x in a single tick. The incremental ramp can't outrun the step-function spike — 560 dropped requests, even though every scaling decision was correct.

### `wrong-direction/`
"Fix" it by scaling far more aggressively — double the fleet on every over-threshold evaluation, halve it on every under-threshold one. Drops fall to 160 (a real improvement), but the fleet balloons to 32 pods for a spike that only ever needed about 10 — 440 wasted pod-ticks, more than 3x the original version's waste.

### `principle-applied/`
Compute the actual target fleet size directly from demand (the real Kubernetes HPA formula), scale straight to that target in one step, evaluate every tick instead of every 2 (removing avoidable delay), and gate scale-down behind a stabilization window. Result: 210 dropped requests — worse than the wrong-direction version's 160, but with less than a third of its wasted capacity and one-tenth the scaling actions. Startup lag is a physical floor; this is the best trade-off, not a drop-free fix.

## Running

Each folder is self-contained. From within a folder:

```bash
javac -Xlint:all *.java
java Main
```

## Key Takeaway

A correctly detecting, correctly reacting autoscaler can still drop requests during a spike, because the gap between "decided to scale" and "capacity is actually ready" is a real, physical delay. Fixing drops by scaling more aggressively just trades the problem for gross over-provisioning. Sizing to the actual computed target and removing avoidable evaluation delay gets a comparably large improvement in drops at a small fraction of the wasted capacity — but it's a better trade-off, not a magic one, because pod startup time remains a hard floor no scaling policy alone eliminates.
