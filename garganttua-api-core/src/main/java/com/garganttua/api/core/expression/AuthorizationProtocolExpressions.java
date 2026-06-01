package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.security.authorization.AuthorizationFormatException;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.reflection.IClass;

import jakarta.annotation.Nullable;

import static com.garganttua.api.core.expression.ExpressionUtils.unwrapOptional;

/**
 * Expressions for the authorization decoding step inside {@code VERIFY_AUTHORIZATION.gs}.
 * The protocol pool is held on {@link IApi}; protocols are routed by the scheme
 * token (first whitespace-delimited word of the {@code Authorization} header).
 */
@Reflected(queryAllPublicMethods = true)
public class AuthorizationProtocolExpressions {

	@Expression(name = "rawAuthorizationAsString",
			description = "Normalises a raw Authorization arg: returns it as String when already String, or UTF-8 decoded when byte[]/Byte[]. Null passes through.")
	public static String rawAuthorizationAsString(@Nullable Object raw) {
		Object value = unwrapOptional(raw);
		if (value == null) return null;
		if (value instanceof String s) return s;
		if (value instanceof byte[] arr) return new String(arr, StandardCharsets.UTF_8);
		if (value instanceof Byte[] boxed) {
			byte[] out = new byte[boxed.length];
			for (int i = 0; i < boxed.length; i++) out[i] = boxed[i];
			return new String(out, StandardCharsets.UTF_8);
		}
		return value.toString();
	}

	@Expression(name = "parseAuthorizationScheme",
			description = "Splits the raw Authorization header on the first whitespace run. Returns the lowercased scheme token. Throws AuthorizationFormatException if the header is null/blank or contains no whitespace.")
	public static String parseAuthorizationScheme(@Nullable Object rawAuth) {
		String raw = stringOrNull(rawAuth);
		if (raw == null) {
			throw new AuthorizationFormatException("Authorization header is null");
		}
		String stripped = raw.stripLeading();
		if (stripped.isEmpty()) {
			throw new AuthorizationFormatException("Authorization header is blank");
		}
		int sep = firstWhitespaceIndex(stripped);
		if (sep < 0) {
			throw new AuthorizationFormatException("Authorization header has no scheme/value separator: " + raw.strip());
		}
		return stripped.substring(0, sep).toLowerCase(Locale.ROOT);
	}

	@Expression(name = "parseAuthorizationValue",
			description = "Returns the portion of the Authorization header AFTER the first whitespace run. Throws AuthorizationFormatException if the value part is missing.")
	public static String parseAuthorizationValue(@Nullable Object rawAuth) {
		String raw = stringOrNull(rawAuth);
		if (raw == null) {
			throw new AuthorizationFormatException("Authorization header is null");
		}
		String stripped = raw.stripLeading();
		if (stripped.isEmpty()) {
			throw new AuthorizationFormatException("Authorization header is blank");
		}
		int sep = firstWhitespaceIndex(stripped);
		if (sep < 0) {
			throw new AuthorizationFormatException("Authorization header has no scheme/value separator: " + raw.strip());
		}
		String value = stripped.substring(sep).strip();
		if (value.isEmpty()) {
			throw new AuthorizationFormatException("Authorization header has no value after the scheme");
		}
		return value;
	}

	@Expression(name = "resolveAuthorizationProtocol",
			description = "Picks the first registered IAuthorizationProtocol whose scheme() matches the given scheme (case-insensitive). Throws if none match.")
	public static IAuthorizationProtocol resolveAuthorizationProtocol(@Nullable Object apiContext, @Nullable Object scheme) {
		IApi api = (IApi) unwrapOptional(apiContext);
		String s = stringOrNull(scheme);
		if (api == null) {
			throw new ApiException("API context is null");
		}
		if (s == null) {
			throw new ApiException("Scheme is null");
		}
		String target = s.toLowerCase(Locale.ROOT);
		for (IAuthorizationProtocol p : api.getAuthorizationProtocols()) {
			if (p.scheme() != null && p.scheme().toLowerCase(Locale.ROOT).equals(target)) {
				return p;
			}
		}
		throw new ApiException("No authorization protocol registered for scheme: " + s);
	}

	@Expression(name = "decodeAuthorization",
			description = "Delegates to IAuthorizationProtocol.decode(value, api). Returns the entity the protocol produces (any user-defined POJO matching the protocol's targetDomain — no interface contract). Errors are wrapped as ApiException.")
	public static Object decodeAuthorization(@Nullable Object protocol, @Nullable Object value, @Nullable Object apiContext) {
		IAuthorizationProtocol p = (IAuthorizationProtocol) unwrapOptional(protocol);
		String v = stringOrNull(value);
		IApi api = (IApi) unwrapOptional(apiContext);
		if (p == null) {
			throw new ApiException("Authorization protocol is null");
		}
		if (v == null) {
			throw new ApiException("Authorization value is null");
		}
		try {
			return p.decode(v, api);
		} catch (ApiException ae) {
			throw ae;
		} catch (RuntimeException e) {
			throw new ApiException("Authorization decoding failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Sentinel key used to hand the resolved {@link IAuthorizationProtocol} from
	 * {@link #decodeRequestAuthorization} to {@link #resolveAuthorizationTargetClass}
	 * within the same {@code VERIFY_AUTHORIZATION} run. Not part of the public
	 * request contract — only the script consumes it, downstream of decoding.
	 */
	private static final String DECODED_PROTOCOL_ARG = "_authzProtocol";

	@Expression(name = "decodeRequestAuthorization",
			description = "Single-call decoder used by VERIFY_AUTHORIZATION.gs. Mode B: returns the authorization entity already on operationRequest.authorization unchanged (no parsing — caller has pre-decoded; the framework treats any non-null Object as a valid token shape and lets the DSL/definition lookup decide what to do with it). Mode A: reads rawAuthorization, parses scheme/value, resolves the matching IAuthorizationProtocol, decodes, stores the resolved protocol back on the request for downstream targetDomain resolution, and returns the decoded entity. Throws AuthorizationFormatException on malformed header (→ 400) and ApiException on missing token / unknown scheme / decode failure (→ 401).")
	public static Object decodeRequestAuthorization(@Nullable Object operationRequest, @Nullable Object apiContext) {
		IOperationRequest req = (IOperationRequest) unwrapOptional(operationRequest);
		if (req == null) {
			throw new ApiException("decodeRequestAuthorization: operationRequest is null");
		}
		// Mode B — caller has already decoded; return as-is, do not touch protocol arg.
		Object existing = req.arg(IOperationRequest.AUTHORIZATION).orElse(null);
		if (existing != null) {
			return existing;
		}
		// Mode A — parse + resolve + decode.
		Object rawArg = req.arg(IOperationRequest.RAW_AUTHORIZATION).orElse(null);
		if (rawArg == null) {
			throw new ApiException("Missing authorization: neither pre-decoded 'authorization' nor 'rawAuthorization' is present on the request");
		}
		String raw = rawAuthorizationAsString(rawArg);
		String scheme = parseAuthorizationScheme(raw);
		String value = parseAuthorizationValue(raw);
		IAuthorizationProtocol protocol = resolveAuthorizationProtocol(apiContext, scheme);
		Object decoded = decodeAuthorization(protocol, value, apiContext);
		// Hand the protocol to resolveAuthorizationTargetClass via the request.
		req.arg(DECODED_PROTOCOL_ARG, protocol);
		return decoded;
	}

	@Expression(name = "resolveAuthorizationTargetClass",
			description = "Returns the IClass<?> identifying the authenticator domain to dispatch sign-verification and validation against. Mode A: pulls the protocol stashed by decodeRequestAuthorization off the request and returns protocol.targetDomain(). Mode B: no protocol — falls back to the runtime class of the supplied authorization entity. Always returns a non-null class.")
	public static IClass<?> resolveAuthorizationTargetClass(@Nullable Object operationRequest, @Nullable Object authorization) {
		IOperationRequest req = (IOperationRequest) unwrapOptional(operationRequest);
		Object authz = unwrapOptional(authorization);
		if (req != null) {
			Object stored = req.arg(DECODED_PROTOCOL_ARG).orElse(null);
			if (stored instanceof IAuthorizationProtocol p) {
				IClass<?> target = p.targetDomain();
				if (target == null) {
					throw new ApiException("IAuthorizationProtocol '" + p.getClass().getName()
							+ "' returned null from targetDomain()");
				}
				return target;
			}
		}
		// Mode B fallback — no protocol path.
		if (authz == null) {
			throw new ApiException("resolveAuthorizationTargetClass: no protocol on request and no authorization instance supplied — cannot resolve target domain");
		}
		return IClass.getClass(authz.getClass());
	}

	// ----- helpers -----

	private static String stringOrNull(Object value) {
		Object unwrapped = unwrapOptional(value);
		if (unwrapped == null) return null;
		if (unwrapped instanceof String s) return s;
		return unwrapped.toString();
	}

	private static int firstWhitespaceIndex(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isWhitespace(s.charAt(i))) return i;
		}
		return -1;
	}
}
