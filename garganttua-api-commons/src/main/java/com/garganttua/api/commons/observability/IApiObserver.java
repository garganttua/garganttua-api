package com.garganttua.api.commons.observability;

/**
 * Observability hook fired by {@code Domain.invoke} at operation
 * boundaries. Register one or more observers via
 * {@code ApiBuilder.observer(...)}; the framework calls
 * {@link #onOperationStart} just before the workflow runs and
 * {@link #onOperationEnd} just after, even when the workflow aborts.
 *
 * <p>Both methods default to no-op so observers only override what they
 * care about — a metrics aggregator typically only needs
 * {@code onOperationEnd}, a tracing exporter that opens a span pairs
 * start/end via {@link OperationEvent#executionUuid()}.
 *
 * <p><b>Exception policy</b>: observer exceptions are caught and logged
 * by the framework — they never break the operation pipeline. The
 * point of an observer is to watch; a broken observer must not turn a
 * successful business operation into a 500.
 *
 * <p><b>Performance</b>: when the observer list is empty (the default,
 * no {@code .observer(...)} call on the builder), the framework skips
 * all event construction. Once at least one observer is registered the
 * full event objects are built per invocation — keep observers cheap.
 */
public interface IApiObserver {

	default void onOperationStart(OperationEvent event) {
	}

	default void onOperationEnd(OperationEvent event) {
	}

}
