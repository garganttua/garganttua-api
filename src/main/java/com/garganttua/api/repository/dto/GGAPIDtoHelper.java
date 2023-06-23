package com.garganttua.api.repository.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIDtoHelper {

	@SuppressWarnings("unchecked")
	public static IGGAPIDTOFactory<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> getFactory( Class<IGGAPIDTOObject<IGGAPIEntity>> dtoClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (IGGAPIDTOFactory<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) ((IGGAPIDTOObject<IGGAPIEntity>) GGAPIDtoHelper.getOneInstance(dtoClass)).getFactory();
	}

	public static IGGAPIDTOObject<IGGAPIEntity> getOneInstance(Class<IGGAPIDTOObject<IGGAPIEntity>> dtoClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<IGGAPIDTOObject<IGGAPIEntity>> constructor;
		constructor = (Constructor<IGGAPIDTOObject<IGGAPIEntity>>) dtoClass.getConstructor();
		IGGAPIDTOObject<IGGAPIEntity> entity = (IGGAPIDTOObject<IGGAPIEntity>) constructor.newInstance();
		return entity;
	}

}
