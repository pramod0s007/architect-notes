# Paper 136: Liveness vs Readiness Probes

Part of *Architect Notes* Volume 25 — Cloud-Native & Kubernetes Architecture.

## Scenario

Kubernetes offers two distinct probe types for a reason: liveness answers "should this process be killed and restarted," readiness answers "can this pod handle a request right now." Using one signal to answer both forces every legitimate startup delay to look identical to an actual hang.

## The Three Versions

### `pressure/`
Only a liveness probe is configured, with a failure threshold (3 ticks) tighter than the pod's genuine warmup time (5 ticks). The pod is restarted before it ever finishes warming up, resetting the clock — 6 restarts over 20 ticks, and it never becomes ready.

### `wrong-direction/`
"Fix" the crash loop by raising the liveness threshold past the warmup time. The restarts stop (0 of them) — but there's still no readiness gate, so the pod is treated as available for traffic from tick 1, and the first 4 requests sent during real warmup fail outright.

### `principle-applied/`
Separate the two concerns: a readiness probe gates traffic (excluding the pod from the load balancer while it's warming up, 0 failed requests), and a liveness probe has its own grace period long enough to cover normal startup, so it never mistakes warmup for a hang (0 restarts).

## Running

Each folder is self-contained. From within a folder:

```bash
javac -Xlint:all *.java
java Main
```

## Key Takeaway

Loosening a liveness threshold to survive startup only stops the destructive action (restart) from misfiring — it does nothing about traffic still being routed to a pod that isn't actually able to serve yet. The two probes should be configured independently, each tuned to the specific failure it exists to catch.
