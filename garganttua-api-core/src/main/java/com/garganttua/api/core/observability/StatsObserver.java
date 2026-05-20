package com.garganttua.api.core.observability;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.garganttua.api.commons.observability.IApiObserver;
import com.garganttua.api.commons.observability.OperationEvent;
import com.garganttua.api.commons.observability.OperationStats;

/**
 * In-memory aggregator suitable for "what's slow on average" overviews.
 * Maintains per-operation totals — count, success/failure breakdown,
 * sum / min / max of durations — keyed by
 * {@code OperationDefinition.toString()}.
 *
 * <p>Lock-free: a {@link ConcurrentHashMap} of {@code Bucket}s where
 * each bucket uses atomic counters and CAS loops on the min/max. Safe
 * under heavy concurrent traffic, no synchronization on the hot path.
 *
 * <p>For percentiles or distribution histograms, pair the framework
 * with a real metrics library (Micrometer, OpenTelemetry) via a thin
 * adapter observer — this class deliberately keeps to averages so it
 * carries no dependency.
 */
public final class StatsObserver implements IApiObserver {

	private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

	@Override
	public void onOperationEnd(OperationEvent event) {
		if (event.operation() == null || event.duration() == null) return;
		String key = event.operation().toString();
		Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
		bucket.record(event.duration(), event.isSuccess());
	}

	/**
	 * Returns an immutable snapshot of every observed operation's
	 * stats. The snapshot is consistent <em>per bucket</em> (each
	 * bucket is read atomically) but not globally — concurrent
	 * recordings may produce a snapshot where some buckets advanced
	 * past others. Good enough for monitoring overviews; not suitable
	 * for invariants that must hold across operations.
	 */
	public Map<String, OperationStats> snapshot() {
		Map<String, OperationStats> result = new java.util.HashMap<>();
		buckets.forEach((key, bucket) -> result.put(key, bucket.snapshot(key)));
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

		OperationStats snapshot(String key) {
			long c = count.get();
			long s = successCount.get();
			return new OperationStats(
					key,
					c,
					s,
					c - s,
					Duration.ofNanos(totalNanos.get()),
					min.get(),
					max.get());
		}
	}
}
