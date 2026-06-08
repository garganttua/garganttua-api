package com.garganttua.api.core.expression;

import java.util.Optional;

import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.core.reflection.IReflection;

/**
 * Shared utility methods used by expression classes.
 */
public class ExpressionUtils {

	static final IReflection REFLECTION = DefaultMapper.reflection();

	public static Object unwrapOptional(Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.orElse(null);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	static <T> Optional<T> unwrap(Object value, Class<T> type) {
		Object unwrapped = value;
		while (unwrapped instanceof Optional<?> opt) {
			if (opt.isEmpty()) return Optional.empty();
			Object inner = opt.get();
			if (type.isInstance(inner)) return Optional.of(type.cast(inner));
			unwrapped = inner;
		}
		return Optional.ofNullable(type.isInstance(unwrapped) ? type.cast(unwrapped) : null);
	}

	public static IDomain<?> toDomain(Object context) {
		return context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
	}

	public static DomainDefinition<?> toDomainDefinition(IDomain<?> dc) {
		if (dc instanceof Domain<?> domCtx) {
			return (DomainDefinition<?>) domCtx.getDomainDefinition();
		}
		return null;
	}

	private ExpressionUtils() {}
}
