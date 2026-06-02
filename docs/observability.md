# Observability — `IApiObserver`

Opt-in observability fired at operation boundaries, with a built-in in-memory aggregator and a zero-overhead hot path when no observer is registered.

Opt-in observability fired by `Domain.invoke` at operation boundaries.

### Registering an observer

```java
ApiBuilder.builder()
    .observer(new StatsObserver())          // built-in in-memory aggregator
    .observer(new MyMicrometerObserver(registry))
    .build();
```

Multiple `.observer(...)` calls add multiple observers — they fire in
registration order. **Without any `.observer(...)` call the framework
skips event construction entirely** — zero overhead on the hot path
beyond an `isEmpty()` check.

### Writing an observer

```java
public class MyObserver implements IApiObserver {
    @Override public void onOperationStart(OperationEvent e) {
        // e.executionUuid is shared with onOperationEnd → use it
        // to pair start/end (e.g. open a span for OpenTelemetry).
    }
    @Override public void onOperationEnd(OperationEvent e) {
        // e.duration / e.code / e.failure populated here.
        // e.isSuccess() returns true on OK/CREATED/UPDATED/DELETED.
    }
}
```

Observer exceptions are caught and logged by the framework — a broken
observer never turns a successful business operation into a 500.

### Built-in `StatsObserver`

In-memory aggregator suitable for "what's slow on average" overviews —
count, success/failure breakdown, sum / min / max / average per
operation key. Lock-free, safe under heavy concurrent traffic.

```java
StatsObserver stats = new StatsObserver();
ApiBuilder.builder().observer(stats).build();

// ... traffic flows ...

Map<String, OperationStats> snapshot = api.getOperationStats();
// keys are OperationDefinition.toString() — e.g. "users-create-one-user"
```

For percentiles, distribution histograms or distributed tracing, wire
a Micrometer / OpenTelemetry adapter observer alongside —
`StatsObserver` carries no external dependency by design.
