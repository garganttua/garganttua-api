package com.garganttua.api.spec;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOFactory;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.repository.dto.SpringCrudifyDtoHelper;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

public class SpringCrudifyDomainable<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> implements ISpringCrudifyDomainable<Entity, Dto> {

	@Inject 
	protected ISpringCrudifyDomain<Entity, Dto> domainObj;

	@Getter
	protected String domain;

	@Getter
	protected ISpringCrudifyEntityFactory<Entity> entityFactory;
	
	@Getter
	protected ISpringCrudifyDTOFactory<Entity, Dto> dtoFactory;

	@Getter
	protected Class<Entity> entityClass;

	@Getter
	protected Class<Dto> dtoClass;
	
	public void setDomain(ISpringCrudifyDomain<Entity, Dto> domain) {
		this.domainObj = domain;
		this.initDomainableObject();
	}
	
	public SpringCrudifyDomainable(ISpringCrudifyDomain<Entity, Dto> domain) {
		this.domainObj = domain;
		this.initDomainableObject();
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	protected void initDomainableObject() {
		this.dtoClass = this.domainObj.getDtoClass();
		this.entityClass = this.domainObj.getEntityClass();
		try {
			
			this.entityFactory = (ISpringCrudifyEntityFactory<Entity>) SpringCrudifyEntityHelper.getFactory((Class<ISpringCrudifyEntity>) this.domainObj.getEntityClass());
			this.dtoFactory = (ISpringCrudifyDTOFactory<Entity, Dto>) SpringCrudifyDtoHelper.getFactory((Class<ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) this.domainObj.getDtoClass());
			
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.domain = this.domainObj.getDomain();
	}
	
}
