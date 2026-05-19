package com.garganttua.api.commons.service;

import java.time.Duration;
import java.util.Optional;

public interface IOperationResponse {

	OperationResponseCode getResponseCode();

	/**
	 * Carries the operation's outcome.
	 * <ul>
	 *   <li>On <strong>success</strong>: the produced payload (the entity,
	 *       the list, the operation output object). Type is operation-defined.</li>
	 *   <li>On <strong>failure</strong>: the {@link Throwable} that caused
	 *       the failure. Callers that only want a message should call
	 *       {@code throwable.getMessage()} or rely on {@code toString()};
	 *       callers that need the full chain can use {@link #getException()}.</li>
	 * </ul>
	 *
	 * <p>This was tightened on 2026-05-19: failures used to pass a bare
	 * String message here, which forced the framework to flatten functional
	 * exceptions into opaque text and discarded the stack trace + cause
	 * chain. Returning the {@code Throwable} preserves the diagnostic
	 * information all the way to the transport layer.
	 */
	Object getResponse();

	/**
	 * End-to-end processing time, measured from {@code Domain.invoke()} entry
	 * to response construction. {@code null} when the response was built
	 * outside an instrumented invocation path (legacy helpers, test fixtures,
	 * responses constructed before reaching the workflow).
	 */
	default Duration getProcessingTime() {
		return null;
	}

	/**
	 * Convenience accessor for the failure path: returns the carried
	 * {@link Throwable} when the response is a failure, empty otherwise.
	 */
	default Optional<Throwable> getException() {
		Object body = getResponse();
		return body instanceof Throwable t ? Optional.of(t) : Optional.empty();
	}

}
