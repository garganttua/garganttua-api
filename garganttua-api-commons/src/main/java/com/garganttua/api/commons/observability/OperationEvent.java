package com.garganttua.api.commons.observability;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.OperationResponseCode;

/**
 * Snapshot passed to an {@link IApiObserver} at operation boundaries.
 *
 * <p>Built once at start and once at end of every {@code Domain.invoke}
 * call when at least one observer is registered. The {@code start}
 * event carries {@code endedAt}, {@code duration}, {@code code} and
 * {@code failure} as {@code null}; the {@code end} event fills them in.
 *
 * <p>Identity is the {@link #executionUuid} — same UUID on the start
 * and end events of a single invocation, so observers that pair them
 * (e.g. a tracing exporter that emits a span) can correlate.
 *
 * @param executionUuid unique id assigned by {@code Domain.invoke}
 * @param domain        the domain name (e.g. {@code "users"})
 * @param operation     the resolved {@link OperationDefinition} when
 *                      one was attached to the request; {@code null}
 *                      when the request had no operation arg (rare —
 *                      typically a build-time mismatch)
 * @param caller        the caller as seen by the pipeline after
 *                      auto-materialization of an anonymous caller;
 *                      never {@code null}
 * @param startedAt     wall-clock start
 * @param endedAt       wall-clock end ({@code null} on the start event)
 * @param duration      end - start ({@code null} on the start event)
 * @param code          response code chosen by the pipeline
 *                      ({@code null} on the start event)
 * @param failure       throwable when the operation aborted, otherwise
 *                      {@code null}; also {@code null} on the start
 *                      event
 */
public record OperationEvent(
        UUID executionUuid,
        String domain,
        OperationDefinition operation,
        ICaller caller,
        Instant startedAt,
        Instant endedAt,
        Duration duration,
        OperationResponseCode code,
        Throwable failure) {

    /**
     * Whether this is the end event of the pair (as opposed to the
     * start event). Convenient sugar for observers that only care
     * about completed operations — sum, count, durations.
     */
    public boolean isEnd() {
        return endedAt != null;
    }

    /**
     * Whether the operation completed with a success-class response
     * code (OK / CREATED / UPDATED / DELETED). Always {@code false} on
     * the start event.
     */
    public boolean isSuccess() {
        if (code == null) return false;
        return switch (code) {
            case OK, CREATED, UPDATED, DELETED -> true;
            default -> false;
        };
    }
}
