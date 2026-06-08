package com.garganttua.api.core.event;

import java.util.Objects;

import com.garganttua.api.commons.event.IEvent;
import com.garganttua.api.commons.event.IEventPublisher;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;

/**
 * Adapts an {@link IEventPublisher} onto the observability stream: it is an
 * {@link IObserver} that, whenever an {@link ObservableEvent} carries an
 * {@link IEvent} payload, forwards it to {@link IEventPublisher#publishEvent}.
 * <p>
 * {@code Domain} subscribes one of these per {@code .events(...)} registration
 * onto its own observable registry at init time, so domain business events flow
 * out through the same fan-out as telemetry — observability is the transport,
 * the {@code IEvent} is the cargo. Observable events without an {@code IEvent}
 * payload (Start markers, workflow {@code stage:*}/{@code script:*} timing) are
 * ignored.
 */
public final class EventPublisherObserver implements IObserver<ObservableEvent> {

	private final IEventPublisher publisher;

	public EventPublisherObserver(IEventPublisher publisher) {
		this.publisher = Objects.requireNonNull(publisher, "Event publisher cannot be null");
	}

	/** The wrapped publisher — exposed for identity assertions/diagnostics. */
	public IEventPublisher getPublisher() {
		return this.publisher;
	}

	@Override
	public void onEvent(ObservableEvent event) {
		if (event != null && event.payload() instanceof IEvent businessEvent) {
			this.publisher.publishEvent(businessEvent);
		}
	}
}
