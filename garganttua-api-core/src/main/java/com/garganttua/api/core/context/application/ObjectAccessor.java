package com.garganttua.api.core.context.application;

import com.garganttua.api.core.context.ContextException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class ObjectAccessor {

	@FunctionalInterface
	public interface ThrowingFunction<T, R> {
		R apply(T t) throws CoreException;
	}

	public static <Infos, ReturnedType> ReturnedType getValue(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod) throws CoreException {
		if (object == null || getInfosClassMethod == null || getFieldAddressMethod == null) {
			throw new ContextException("null parameter");
		}
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			return ObjectAccessor.getValue(object, infos, getFieldAddressMethod);
		} catch (Exception e) {
			CoreException.processException(e);
			return null;
		}
	}

	public static <Infos, SetType> void setValue(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod, SetType value) throws CoreException {
		if (object == null || getInfosClassMethod == null || getFieldAddressMethod == null) {
			throw new ContextException("null parameter");
		}
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			ObjectAccessor.setValue(object, infos, getFieldAddressMethod, value);
		} catch (Exception e) {
			CoreException.processException(e);
		}
	}

	public static <Infos> Object invoke(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getMethoddAddressMethod, Object... parameters)
			throws CoreException {
		if (object == null || getInfosClassMethod == null || getMethoddAddressMethod == null) {
			throw new ContextException("null parameter");
		}
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			return ObjectAccessor.invoke(object, infos, getMethoddAddressMethod, parameters);
		} catch (Exception e) {
			CoreException.processException(e);
			return null;
		}
	}

	public static <Infos, ReturnedType> ReturnedType getValue(Object object,
			Infos infosKeeper,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod) throws CoreException {
		if (object == null || infosKeeper == null || getFieldAddressMethod == null) {
			throw new ContextException("null parameter");
		}
		try {
			GGObjectAddress fieldAddress = getFieldAddressMethod.apply(infosKeeper);

			return (ReturnedType) GGObjectQueryFactory.objectQuery(object).getValue(fieldAddress);
		} catch (Exception e) {
			CoreException.processException(e);
			return null;
		}
	}

	public static <Infos, SetType> void setValue(Object object,
			Infos infosKeeper,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod, SetType value) throws CoreException {
		if (object == null || infosKeeper == null || getFieldAddressMethod == null) {
			throw new ContextException("null parameter");
		}

		try {
			GGObjectAddress fieldAddress = getFieldAddressMethod.apply(infosKeeper);

			GGObjectQueryFactory.objectQuery(object).setValue(fieldAddress, value);
		} catch (Exception e) {
			CoreException.processException(e);
		}
	}

	public static <Infos> Object invoke(Object object,
			Infos infosKeeper,
			ThrowingFunction<Infos, GGObjectAddress> getMethoddAddressMethod, Object... parameters)
			throws CoreException {
		if (object == null || infosKeeper == null || getMethoddAddressMethod == null) {
			throw new ContextException("null parameter");
		}

		try {

			GGObjectAddress methodAddress = getMethoddAddressMethod.apply(infosKeeper);

			return GGObjectQueryFactory.objectQuery(object).invoke(methodAddress, parameters);
		} catch (Exception e) {
			CoreException.processException(e);
			return null;
		}
	}
}
