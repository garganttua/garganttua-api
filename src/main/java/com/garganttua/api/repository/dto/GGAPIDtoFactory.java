package com.garganttua.api.repository.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDtoFactory {

	@SuppressWarnings("unchecked")
	public static <Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> Dto getOneInstance(String tenantId, Entity entity) throws GGAPIEntityException  {
		Constructor<Dto> constructor;
		GGAPIDynamicDomain dDomain;
		try {
			dDomain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
			constructor = (Constructor<Dto>) dDomain.dtoClass.getConstructor(String.class, dDomain.entityClass);
			Dto dto = constructor.newInstance(tenantId, entity);
			
			return dto;
		} catch (GGAPIEngineException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.warn("Error during Dto instance creation");
			if( log.isDebugEnabled() ) {
				log.warn("The error :",e);
			}
			throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Error during Dto instance creation", e);
		}
		
	}

}
