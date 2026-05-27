package com.garganttua.api.core.observability;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.garganttua.api.commons.observability.OperationStats;
import com.garganttua.core.observability.EndEvent;
import com.garganttua.core.observability.ErrorEvent;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;

/**
 * In-memory aggregator that subscribes to core's {@link ObservableEvent}
 * stream and tallies per-operation totals — count, success/failure breakdown,
 * sum / min / max of durations — keyed by the event's {@code source}.
 *
 * <p>{@link EndEvent}s count as successes; {@link ErrorEvent}s count as
 * failures. {@code StartEvent}s are ignored — durations come straight off
 * the End/Error event payload.
 *
 * <p>Lock-free: a {@link ConcurrentHashMap} of {@code Bucket}s where each
 * bucket uses atomic counters and CAS loops on min/max. Safe under heavy
 * concurrent traffic, no synchronization on the hot path.
 *
 * <p>Filter the api-operation slice from a wider observability feed via
 * the source prefix: register with
 * {@code observability.subscribe(stats).matchingAnySource("api:operation:*")}
 * or with {@code @Observer(sources = "api:operation:*")} when discovery
 * is annotation-driven.
 *
 * <p>For percentiles or distribution histograms, pair the framework with a
 * real metrics library (Micrometer, OpenTelemetry) via a thin adapter
 * observer — this class deliberately keeps to averages so it carries no
 * dependency.
 */
public final class StatsObserver implements IObserver<ObservableEvent> {

	private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

	@Override
	public void onEvent(ObservableEvent event) {
		switch (event) {
			case EndEvent end -> record(end.source(), end.duration(), true);
			case ErrorEvent err -> record(err.source(), err.duration(), false);
			default -> { /* StartEvent and any future variant: nothing to tally */ }
		}
	}

	private void record(String source, Duration duration, boolean success) {
		if (source == null || duration == null) return;
		Bucket bucket = buckets.computeIfAbsent(source, k -> new Bucket());
		bucket.record(duration, success);
	}

	/**
	 * Returns an immutable snapshot of every observed source's stats. The
	 * snapshot is consistent <em>per bucket</em> (each bucket is read
	 * atomically) but not globally — concurrent recordings may produce a
	 * snapshot where some buckets advanced past others. Good enough for
	 * monitoring overviews; not suitable for invariants that must hold
	 * across sources.
	 */
	public Map<String, OperationStats> snapshot() {
		Map<String, OperationStats> result = new java.util.HashMap<>();
		buckets.forEach((source, bucket) -> result.put(source, bucket.snapshot(source)));
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Clears every aggregated bucket. Useful in tests; also a viable
	 * "reset window" trigger for callers that want rolling stats.
	 */
	public void reset() {
		this.buckets.clear();
	}

	private static final class Bucket {
		private final AtomicLong count = new AtomicLong();
		private final AtomicLong successCount = new AtomicLong();
		private final AtomicLong totalNanos = new AtomicLong();
		private final AtomicReference<Duration> min = new AtomicReference<>();
		private final AtomicReference<Duration> max = new AtomicReference<>();

		void record(Duration duration, boolean success) {
			count.incrementAndGet();
			if (success) successCount.incrementAndGet();
			totalNanos.addAndGet(duration.toNanos());
			updateMin(duration);
			updateMax(duration);
		}

		private void updateMin(Duration candidate) {
			min.updateAndGet(current ->
					current == null || candidate.compareTo(current) < 0 ? candidate : current);
		}

		private void updateMax(Duration candidate) {
			max.updateAndGet(current ->
					current == null || candidate.compareTo(current) > 0 ? candidate : current);
		}

		OperationStats snapshot(String source) {
			long c = count.get();
			long s = successCount.get();
			return new OperationStats(
					source,
					c,
					s,
					c - s,
					Duration.ofNanos(totalNanos.get()),
					min.get(),
					max.get());
		}
	}
}
