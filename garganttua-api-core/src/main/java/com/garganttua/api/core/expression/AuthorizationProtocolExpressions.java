package com.garganttua.api.core.expression;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.security.authorization.IAuthorization;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.core.expression.annotations.Expression;

import jakarta.annotation.Nullable;

import static com.garganttua.api.core.expression.ExpressionUtils.unwrapOptional;

/**
 * Expressions for the authorization decoding step inside {@code VERIFY_AUTHORIZATION.gs}.
 * The protocol pool is held on {@link IApi}; protocols are routed by the scheme
 * token (first whitespace-delimited word of the {@code Authorization} header).
 */
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
			description = "Splits the raw Authorization header on the first whitespace run. Returns the lowercased scheme token. Throws if the header is null/blank or contains no whitespace.")
	public static String parseAuthorizationScheme(@Nullable Object rawAuth) {
		String raw = stringOrNull(rawAuth);
		if (raw == null) {
			throw new ApiException("Authorization header is null");
		}
		String stripped = raw.stripLeading();
		if (stripped.isEmpty()) {
			throw new ApiException("Authorization header is blank");
		}
		int sep = firstWhitespaceIndex(stripped);
		if (sep < 0) {
			throw new ApiException("Authorization header has no scheme/value separator: " + raw.strip());
		}
		return stripped.substring(0, sep).toLowerCase(Locale.ROOT);
	}

	@Expression(name = "parseAuthorizationValue",
			description = "Returns the portion of the Authorization header AFTER the first whitespace run. Throws if the value part is missing.")
	public static String parseAuthorizationValue(@Nullable Object rawAuth) {
		String raw = stringOrNull(rawAuth);
		if (raw == null) {
			throw new ApiException("Authorization header is null");
		}
		String stripped = raw.stripLeading();
		if (stripped.isEmpty()) {
			throw new ApiException("Authorization header is blank");
		}
		int sep = firstWhitespaceIndex(stripped);
		if (sep < 0) {
			throw new ApiException("Authorization header has no scheme/value separator: " + raw.strip());
		}
		String value = stripped.substring(sep).strip();
		if (value.isEmpty()) {
			throw new ApiException("Authorization header has no value after the scheme");
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
			description = "Delegates to IAuthorizationProtocol.decode(value, api). Errors are wrapped as ApiException.")
	public static IAuthorization decodeAuthorization(@Nullable Object protocol, @Nullable Object value, @Nullable Object apiContext) {
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
