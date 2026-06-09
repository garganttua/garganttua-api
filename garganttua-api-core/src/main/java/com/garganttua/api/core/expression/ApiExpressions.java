package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Optional;

import com.garganttua.core.expression.annotations.Expression;

import jakarta.annotation.Nullable;

/**
 * General-purpose utility expressions used across workflow scripts.
 * <p>
 * CRUD operations are in {@link CrudExpressions},
 * entity lifecycle in {@link EntityLifecycleExpressions},
 * and security in {@link SecurityExpressions}.
 */
@Reflected(queryAllPublicMethods = true)
public class ApiExpressions {

	// Internal helper backing isNull only. The `notNull` / `equals` / `and` names
	// now resolve to core's Optional-aware condition primitives (NotNullCondition,
	// EqualsCondition, AndCondition in garganttua-condition), so those expressions
	// are no longer registered here — kept private as isNull still calls it.
	private static boolean notNull(@Nullable Object value) {
		if (value == null) return false;
		if (value instanceof Optional<?> opt) return opt.isPresent();
		return true;
	}

	@Expression(name = "isNull", description = "Returns true if the value is null or an empty Optional (inverse of notNull)")
	public static boolean isNull(@Nullable Object value) {
		return !notNull(value);
	}

	@Expression(name = "requirePresent", description = "Throws ApiException if value is null or an empty Optional, otherwise returns the unwrapped value")
	public static Object requirePresent(@Nullable Object value) {
		if (value == null) throw new com.garganttua.api.commons.ApiException("Required value is null");
		if (value instanceof Optional<?> opt) {
			return opt.orElseThrow(() -> new com.garganttua.api.commons.ApiException("Required value is empty"));
		}
		return value;
	}

	@Expression(name = "optionalGet", description = "Unwraps an Optional, throwing NoSuchElementException if empty")
	public static Object optionalGet(@Nullable Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.get();
		}
		return value;
	}

	@Expression(name = "coalesce", description = "Returns the first value when it is non-null (a present Optional is unwrapped), otherwise the second. Used to prefer a transport/encoded form over the raw entity when one was produced.")
	public static @Nullable Object coalesce(@Nullable Object preferred, @Nullable Object fallback) {
		if (notNull(preferred)) {
			return preferred instanceof Optional<?> opt ? opt.get() : preferred;
		}
		return fallback;
	}
}
