package com.garganttua.api.spec;

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Autowired;

import com.garganttua.api.repository.dto.GGAPIDtoHelper;
import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

public class GGAPIDomainable<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> implements IGGAPIDomainable<Entity, Dto> {

//	@Autowired 
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
	
//	public void setDomain(IGGAPIDomain<Entity, Dto> domain) {
//		this.domainObj = domain;
//		this.initDomainableObject();
//	}
	
//	public GGAPIDomainable(IGGAPIDomain<Entity, Dto> domain) {
//		this.domainObj = domain;
//		this.initDomainableObject();
//	}
	
	@SuppressWarnings("unchecked")
	public void setDomain(IGGAPIDomain<Entity, Dto> domain) {
		this.domainObj = domain;
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
