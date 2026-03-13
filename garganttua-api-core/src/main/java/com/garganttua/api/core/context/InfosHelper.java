package com.garganttua.api.core.context;

import java.util.function.Function;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;

public class InfosHelper {

	private static final IReflection REFLECTION = DefaultMapper.reflection();

	@FunctionalInterface
	public interface CheckedFunction<T, R> {
		R apply(T t) throws CoreException;
	}

	@SuppressWarnings("unchecked")
	public static <R, V> V getValue(Object entity, CheckedFunction<Class<?>, R> checker,
			Function<R, ObjectAddress> accessor) throws CoreException {
		try {
			R infos = checker.apply(entity.getClass());
			ObjectAddress address = accessor.apply(infos);
			return (V) REFLECTION.getFieldValue(entity, address);
		} catch (ReflectionException e) {
			throw new SecurityException("Failed to get value from entity", e);
		}
	}

	public static <R> void setValue(Object entity, CheckedFunction<Class<?>, R> checker,
			Function<R, ObjectAddress> accessor, Object value) throws CoreException {
		try {
			R infos = checker.apply(entity.getClass());
			ObjectAddress address = accessor.apply(infos);
			REFLECTION.setFieldValue(entity, address, value);
		} catch (ReflectionException e) {
			throw new SecurityException("Failed to set value on entity", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <R> Object invoke(Object entity, CheckedFunction<Class<?>, R> checker,
			Function<R, ObjectAddress> accessor, Object... args) throws CoreException {
		try {
			R infos = checker.apply(entity.getClass());
			ObjectAddress address = accessor.apply(infos);
			return REFLECTION.invokeDeep(entity, address, null, args).single();
		} catch (ReflectionException e) {
			throw new SecurityException("Failed to invoke method on entity", e);
		}
	}
}
