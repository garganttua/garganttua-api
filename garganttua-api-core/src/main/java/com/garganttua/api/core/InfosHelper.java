package com.garganttua.api.core;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class InfosHelper {

	@FunctionalInterface
	public interface ThrowingFunction<T, R> {
	    R apply(T t) throws CoreException;
	}

	@SuppressWarnings("unchecked")
	public static <Infos, ReturnedType> ReturnedType getValue(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod) throws CoreException {
		if (object == null || getInfosClassMethod == null || getFieldAddressMethod == null) {
            throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "null parameter");
        }
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			GGObjectAddress fieldAddress = getFieldAddressMethod.apply(infos);
			
			return (ReturnedType) GGObjectQueryFactory.objectQuery(object).getValue(fieldAddress);
		} catch (Exception e) {
			CoreException.processException(e);
			return null;
		}
	}
	
	public static <Infos, SetType> void setValue(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod, SetType value) throws CoreException {
		if (object == null || getInfosClassMethod == null || getFieldAddressMethod == null) {
            throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "null parameter");
        }
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			GGObjectAddress fieldAddress = getFieldAddressMethod.apply(infos);
			
			GGObjectQueryFactory.objectQuery(object).setValue(fieldAddress, value);
		} catch (Exception e) {
			CoreException.processException(e);
		}
	}
	
	public static <Infos> Object invoke(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getMethoddAddressMethod, Object ...parameters) throws CoreException {
		if (object == null || getInfosClassMethod == null || getMethoddAddressMethod == null) {
            throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "null parameter");
        }
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			GGObjectAddress methodAddress = getMethoddAddressMethod.apply(infos);
			
			return GGObjectQueryFactory.objectQuery(object).invoke(methodAddress, parameters);
		} catch (Exception e) {
			CoreException.processException(e);
			return null;
		}
	}
}
