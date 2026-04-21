package com.garganttua.api.core.expression;

import java.util.Objects;
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
public class ApiExpressions {

	@Expression(name = "notNull", description = "Returns true if the value is not null and not an empty Optional")
	public static boolean notNull(@Nullable Object value) {
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
		if (value == null) throw new com.garganttua.api.spec.ApiException("Required value is null");
		if (value instanceof Optional<?> opt) {
			return opt.orElseThrow(() -> new com.garganttua.api.spec.ApiException("Required value is empty"));
		}
		return value;
	}

	@Expression(name = "and", description = "Logical AND of two boolean values")
	public static boolean andExpr(boolean a, boolean b) {
		return a && b;
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal")
	public static boolean equalsExpr(@Nullable Object a, @Nullable Object b) {
		return Objects.equals(ExpressionUtils.unwrapOptional(a), ExpressionUtils.unwrapOptional(b));
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal (boolean variant)")
	public static boolean equalsExprBool(@Nullable Object a, boolean b) {
		return Objects.equals(ExpressionUtils.unwrapOptional(a), b);
	}

	@Expression(name = "optionalGet", description = "Unwraps an Optional, throwing NoSuchElementException if empty")
	public static Object optionalGet(@Nullable Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.get();
		}
		return value;
	}
}
