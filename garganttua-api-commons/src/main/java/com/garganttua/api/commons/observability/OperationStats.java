package com.garganttua.api.commons.observability;

import java.time.Duration;

/**
 * Snapshot of timing aggregates for a single operation. Returned by
 * {@code StatsObserver.snapshot()} — a built-in
 * {@code IObserver<ObservableEvent>} aggregator the user holds and
 * registers either via core's {@code ObservabilityBuilder.subscribe(...)}
 * or by annotating their subclass with {@code @Observer}.
 *
 * <p>All fields are immutable values — the snapshot is a point-in-time
 * copy of the live aggregator state.
 *
 * @param operationKey  source string of the form
 *                      {@code "api:operation:<domain>:<op>"}
 * @param count         total number of completed invocations
 * @param successCount  invocations that ended with an {@code EndEvent}
 *                      (errors are counted as failures)
 * @param failureCount  count minus successCount
 * @param totalDuration sum of all durations
 * @param minDuration   smallest observed duration; {@code null} when
 *                      {@link #count} is zero
 * @param maxDuration   largest observed duration; {@code null} when
 *                      {@link #count} is zero
 */
public record OperationStats(
        String operationKey,
        long count,
        long successCount,
        long failureCount,
        Duration totalDuration,
        Duration minDuration,
        Duration maxDuration) {

    /**
     * Mean duration. {@link Duration#ZERO} when {@link #count} is zero.
     */
    public Duration averageDuration() {
        if (count == 0 || totalDuration == null) return Duration.ZERO;
        return totalDuration.dividedBy(count);
    }
}
