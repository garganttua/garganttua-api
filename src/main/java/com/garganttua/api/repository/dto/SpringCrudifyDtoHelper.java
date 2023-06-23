package com.garganttua.api.repository.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.garganttua.api.spec.ISpringCrudifyEntity;

public class SpringCrudifyDtoHelper {

	@SuppressWarnings("unchecked")
	public static ISpringCrudifyDTOFactory<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> getFactory( Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>> dtoClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (ISpringCrudifyDTOFactory<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) ((ISpringCrudifyDTOObject<ISpringCrudifyEntity>) SpringCrudifyDtoHelper.getOneInstance(dtoClass)).getFactory();
	}

	public static ISpringCrudifyDTOObject<ISpringCrudifyEntity> getOneInstance(Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>> dtoClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<ISpringCrudifyDTOObject<ISpringCrudifyEntity>> constructor;
		constructor = (Constructor<ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) dtoClass.getConstructor();
		ISpringCrudifyDTOObject<ISpringCrudifyEntity> entity = (ISpringCrudifyDTOObject<ISpringCrudifyEntity>) constructor.newInstance();
		return entity;
	}

}
