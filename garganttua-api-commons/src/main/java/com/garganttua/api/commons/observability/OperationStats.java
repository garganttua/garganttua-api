package com.garganttua.api.commons.observability;

import java.time.Duration;

/**
 * Snapshot of timing aggregates for a single operation. Returned by
 * {@code IApi.getOperationStats()} when a built-in
 * {@link com.garganttua.api.commons.observability.IApiObserver
 * StatsObserver} is registered.
 *
 * <p>All fields are immutable values — the snapshot is a point-in-time
 * copy of the live aggregator state.
 *
 * @param operationKey  {@code OperationDefinition.toString()} — e.g.
 *                      {@code "users-create-one-user"}
 * @param count         total number of completed invocations
 * @param successCount  invocations that ended with an OK / CREATED /
 *                      UPDATED / DELETED code
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
