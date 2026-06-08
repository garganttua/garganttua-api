package com.garganttua.api.binding.javalin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.core.reflection.IClass;

import io.javalin.http.Context;

/**
 * Transport adapter bridging a Javalin {@link Context} to the garganttua-api
 * pipeline — the companion of {@link JavalinInterface}.
 * <p>
 * {@code JavalinInterface} owns the server and the routing; it hands the live
 * {@code Context} to the pipeline as {@code rawRequest}. This protocol is then
 * resolved by request type (stage 1, EXTRACT) to pull the transport fields out of
 * the {@code Context}, and again (stage 10, RESPONSE) to write the pipeline's
 * output back onto it.
 * <p>
 * Register it once on the API alongside the interface:
 * <pre>{@code
 *   ApiBuilder.builder()
 *       .protocol(new JavalinProtocol())
 *       .domain(User.class)
 *           .interfasse(new JavalinInterface(7000))
 *           ...
 * }</pre>
 *
 * <h2>Caller seeding</h2>
 * {@link #getCaller} reads the optional transport headers {@code X-Tenant-Id},
 * {@code X-Owner-Id} and {@code X-Caller-Id}. When none is present the caller is
 * {@link JavalinCaller#anonymous() anonymous}: the authenticated principal of a
 * token-bearing request is resolved server-side from the decoded
 * {@code Authorization} header by {@code VERIFY_AUTHORIZATION}, not here. This
 * adapter never trusts {@code superTenant}/{@code superOwner} from the wire — they
 * default to {@code false} and are recomputed server-side.
 */
public class JavalinProtocol implements IProtocol<Context, Context> {

	/** Header carrying the tenant the caller acts within. */
	public static final String TENANT_HEADER = "X-Tenant-Id";
	/** Header carrying the owner the caller acts within. */
	public static final String OWNER_HEADER = "X-Owner-Id";
	/** Header carrying the caller's own id. */
	public static final String CALLER_HEADER = "X-Caller-Id";

	@Override
	public IClass<Context> requestType() {
		return IClass.getClass(Context.class);
	}

	@Override
	public ICaller getCaller(Context ctx) throws ApiException {
		String tenantId = ctx.header(TENANT_HEADER);
		String ownerId = ctx.header(OWNER_HEADER);
		String callerId = ctx.header(CALLER_HEADER);
		if (tenantId == null && ownerId == null && callerId == null) {
			return Caller.createAnonymousCaller();
		}
		// Record component order is (tenantId, requestedTenantId, callerId, ownerId, …).
		// superTenant/superOwner are NEVER asserted by the transport — server-authoritative.
		return new Caller(tenantId, tenantId, callerId, ownerId, false, false, List.of());
	}

	@Override
	public byte[] getRawBody(Context ctx) throws ApiException {
		byte[] body = ctx.bodyAsBytes();
		// Honour the contract: null (not an empty array) for a bodyless request.
		return (body == null || body.length == 0) ? null : body;
	}

	@Override
	public String getAuthorization(Context ctx) throws ApiException {
		return ctx.header("Authorization");
	}

	@Override
	public String getContentType(Context ctx) throws ApiException {
		return ctx.header("Content-Type");
	}

	@Override
	public String getAccept(Context ctx) throws ApiException {
		return ctx.header("Accept");
	}

	@Override
	public String getPath(Context ctx) throws ApiException {
		return ctx.path();
	}

	@Override
	public String getMethod(Context ctx) throws ApiException {
		// HandlerType → its canonical verb name ("GET", "POST", …).
		return ctx.method().name();
	}

	@Override
	public Map<String, String> getQueryParameters(Context ctx) throws ApiException {
		Map<String, String> collapsed = new HashMap<>();
		for (Map.Entry<String, List<String>> e : ctx.queryParamMap().entrySet()) {
			List<String> values = e.getValue();
			if (values != null && !values.isEmpty()) {
				collapsed.put(e.getKey(), values.get(0));
			}
		}
		return collapsed;
	}

	@Override
	public Context buildResponse(Context ctx, Object output, int statusCode) throws ApiException {
		return buildResponse(ctx, output, statusCode, null);
	}

	@Override
	public Context buildResponse(Context ctx, Object output, int statusCode, String contentType) throws ApiException {
		ctx.status(statusCode);
		// Label the body with the negotiated media type — Javalin's result(byte[])
		// otherwise defaults to text/plain, mislabelling a JSON (or XML, …) payload.
		if (contentType != null && !contentType.isBlank()) {
			ctx.contentType(contentType);
		}
		if (output instanceof byte[] bytes) {
			ctx.result(bytes);
		} else if (output != null) {
			ctx.result(String.valueOf(output));
		}
		// else: no body (e.g. a 204/empty pipeline output) — status only.
		return ctx;
	}
}
