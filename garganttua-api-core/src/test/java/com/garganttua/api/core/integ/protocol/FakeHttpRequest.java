package com.garganttua.api.core.integ.protocol;

import java.util.Map;

/**
 * Minimal HTTP-shaped record used to exercise the protocol pipeline without
 * pulling in a Servlet container. Consumed by {@code FakeHttpProtocol} in
 * {@code ProtocolIntegrationTest}.
 */
public record FakeHttpRequest(
		String path,
		String method,
		byte[] body,
		String contentType,
		String accept,
		String authorization,
		String tenantId,
		String callerId,
		Map<String, String> queryParameters
) {
}
