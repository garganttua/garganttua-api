package com.garganttua.api.commons.service;

import java.time.Duration;

public interface IOperationResponse {

	OperationResponseCode getResponseCode();

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

}
