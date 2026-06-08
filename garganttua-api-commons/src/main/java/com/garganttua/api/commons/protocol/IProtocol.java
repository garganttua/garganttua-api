package com.garganttua.api.commons.protocol;

import java.util.Map;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.core.reflection.IClass;

/**
 * Transport protocol adapter for pipeline stages 1 (extract) and 10 (response).
 * <p>
 * An {@code IProtocol} bridges a raw transport object (HTTP {@code ServletRequest},
 * Javalin {@code Context}, a {@code byte[]} wire payload, a gRPC call, …) to the
 * structured {@link com.garganttua.api.commons.service.IOperationRequest} fields consumed
 * by the rest of the pipeline, and symmetrically builds the transport response from
 * the pipeline's final output.
 * <p>
 * The framework discriminates protocols by the raw request's Java class, matching
 * {@link #requestType()} via {@code isAssignableFrom}. Registration order acts as
 * priority: register more specific protocols before generic ones.
 * <p>
 * Implementations are registered either:
 * <ul>
 *   <li>manually via {@code IApiBuilder.protocol(IProtocol)} or
 *       {@code IApiBuilder.protocol(ISupplierBuilder)}, or</li>
 *   <li>automatically by placing the {@link Protocol} annotation on the class and
 *       enabling {@code IApiBuilder.autoDetect(true)} with a scanned package.</li>
 * </ul>
 *
 * @param <REQ> the transport request type this protocol handles
 * @param <RES> the transport response type this protocol produces
 */
public interface IProtocol<REQ, RES> {

	/** The transport request class this protocol handles (used for routing). */
	IClass<REQ> requestType();

	/**
	 * Build an {@link ICaller} from the raw request (principal, tenantId, authorities).
	 * Implementations typically parse the Authorization token or well-known headers.
	 */
	ICaller getCaller(REQ request) throws ApiException;

	/** Raw body bytes; {@code null} for bodyless requests (GET, DELETE, …). */
	byte[] getRawBody(REQ request) throws ApiException;

	/** Raw authorization token (e.g. {@code "Bearer …"}); {@code null} when absent. */
	String getAuthorization(REQ request) throws ApiException;

	/** Content-Type header; {@code null} when absent. */
	String getContentType(REQ request) throws ApiException;

	/** Accept header; {@code null} when absent. */
	String getAccept(REQ request) throws ApiException;

	/** Request path (e.g. {@code "/users/42"}). */
	String getPath(REQ request) throws ApiException;

	/** HTTP method or equivalent verb ({@code "GET"}, {@code "POST"}, …). */
	String getMethod(REQ request) throws ApiException;

	/** Query-string parameters; multi-value entries collapsed to the first value. */
	Map<String, String> getQueryParameters(REQ request) throws ApiException;

	/**
	 * Build the transport response. {@code output} is either a {@code byte[]}
	 * (produced by the serialize stage when {@code Accept} was set) or the raw
	 * pipeline output object when serialization was skipped. The {@code statusCode}
	 * is the HTTP-style code resolved by the exit-code stage.
	 */
	RES buildResponse(REQ request, Object output, int statusCode) throws ApiException;

	/**
	 * Build the transport response, labelling it with the negotiated media type.
	 * {@code contentType} is the wire MIME string of the
	 * {@link com.garganttua.api.commons.serialization.ISerializer} actually chosen on
	 * the response side (e.g. {@code "application/json"}), or {@code null} when
	 * serialization was skipped (no {@code Accept}). Implementations that carry a
	 * content-type header (HTTP) should set it from this value so the response label
	 * follows the serializer that produced the body — true content negotiation rather
	 * than an {@code Accept} heuristic.
	 * <p>
	 * Defaults to {@link #buildResponse(Object, Object, int)} for protocols that do
	 * not model a content type.
	 */
	default RES buildResponse(REQ request, Object output, int statusCode, String contentType) throws ApiException {
		return buildResponse(request, output, statusCode);
	}
}
