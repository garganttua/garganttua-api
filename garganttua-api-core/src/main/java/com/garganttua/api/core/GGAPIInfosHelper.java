package com.garganttua.api.core;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIInfosHelper {

	@FunctionalInterface
	public interface ThrowingFunction<T, R> {
	    R apply(T t) throws GGAPIException;
	}

	@SuppressWarnings("unchecked")
	public static <Infos, ReturnedType> ReturnedType getValue(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod) throws GGAPIException {
		if (object == null || getInfosClassMethod == null || getFieldAddressMethod == null) {
            throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "null parameter");
        }
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			GGObjectAddress fieldAddress = getFieldAddressMethod.apply(infos);
			
			return (ReturnedType) GGObjectQueryFactory.objectQuery(object).getValue(fieldAddress);
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}
	
	public static <Infos, SetType> void setValue(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getFieldAddressMethod, SetType value) throws GGAPIException {
		if (object == null || getInfosClassMethod == null || getFieldAddressMethod == null) {
            throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "null parameter");
        }
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			GGObjectAddress fieldAddress = getFieldAddressMethod.apply(infos);
			
			GGObjectQueryFactory.objectQuery(object).setValue(fieldAddress, value);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}
	
	public static <Infos> Object invoke(Object object,
			ThrowingFunction<Class<?>, Infos> getInfosClassMethod,
			ThrowingFunction<Infos, GGObjectAddress> getMethoddAddressMethod, Object ...parameters) throws GGAPIException {
		if (object == null || getInfosClassMethod == null || getMethoddAddressMethod == null) {
            throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "null parameter");
        }
		Infos infos;
		try {
			infos = getInfosClassMethod.apply(object.getClass());
			GGObjectAddress methodAddress = getMethoddAddressMethod.apply(infos);
			
			return GGObjectQueryFactory.objectQuery(object).invoke(methodAddress, parameters);
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}
}
