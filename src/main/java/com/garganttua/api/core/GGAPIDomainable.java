package com.garganttua.api.core;

import java.lang.reflect.InvocationTargetException;

import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.repository.dto.GGAPIDtoHelper;
import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

import lombok.Getter;

public class GGAPIDomainable<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> implements IGGAPIDomainable<Entity, Dto> {

	protected IGGAPIDomain<Entity, Dto> domainObj;

	@Getter
	protected String domain;

	@Getter
	protected IGGAPIEntityFactory<Entity> entityFactory;
	
	@Getter
	protected IGGAPIDTOFactory<Entity, Dto> dtoFactory;

	@Getter
	protected Class<Entity> entityClass;

	@Getter
	protected Class<Dto> dtoClass;

	@Getter
	protected GGAPIDynamicDomain dynamicDomain;
	
	@SuppressWarnings("unchecked")
	public void setDomain(GGAPIDynamicDomain domain) {
		this.dynamicDomain = domain;
		this.domainObj = (IGGAPIDomain<Entity, Dto>) domain.getDomain();
		this.dtoClass = this.domainObj.getDtoClass();
		this.entityClass = this.domainObj.getEntityClass();
		try {
			
			this.entityFactory = (IGGAPIEntityFactory<Entity>) GGAPIEntityHelper.getFactory((Class<IGGAPIEntity>) this.domainObj.getEntityClass());
			this.dtoFactory = (IGGAPIDTOFactory<Entity, Dto>) GGAPIDtoHelper.getFactory((Class<IGGAPIDTOObject<IGGAPIEntity>>) this.domainObj.getDtoClass());
			
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.domain = this.domainObj.getDomain();
	}
	
}
