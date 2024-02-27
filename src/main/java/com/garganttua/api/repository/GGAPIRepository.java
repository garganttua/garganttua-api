/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.GGAPIDtoFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableMongoRepositories
public class GGAPIRepository implements IGGAPIRepository {

	protected IGGAPIDAORepository daoRepository;

	@Override
    public long getCount(GGAPIDynamicDomain domain, IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc) {
    	log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Get Total Count, Filter {}", caller.getRequestedTenantId(), domain, filter);
    	long totalCount = 0;
    	
    	totalCount = this.daoRepository.count(domain, GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, filter), geoloc);
    	
    	return totalCount;
    }

	@Override
	public boolean doesExist(GGAPIDynamicDomain domain, IGGAPICaller caller, String uuid) {
		
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Checking if entity with uuid {} exists.", caller.getRequestedTenantId(), domain);
		
		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dto = this.daoRepository.find(domain, null, getFilterFromCallerInfosAndDomainInfos(caller, domain, getUuidFilter(uuid)), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+"Entity with uuid "+uuid+" exists.");
			return true;
		} 
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Entity with uuid "+uuid+" does not exists.", caller.getRequestedTenantId(), domain);
		return false;
	}
	
	@Override
	public <Entity extends IGGAPIEntity> boolean doesExist(IGGAPICaller caller, Entity entity) throws GGAPIEngineException {
		if( entity.isGotFromRepository() ) {
			return true;
		}
		if( entity.getUuid() == null || entity.getUuid().isEmpty() ) {
			return false;
		}
		
		GGAPIDynamicDomain domain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
 		
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Checking if entity with uuid "+entity.getUuid()+" exists.", caller.getRequestedTenantId(), domain);
		
		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dto = this.daoRepository.find(domain, null, getFilterFromCallerInfosAndDomainInfos(caller, domain, getUuidFilter(entity.getUuid())), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Entity with uuid "+entity.getUuid()+" exists.");
			return true;
		} 
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Entity with uuid "+entity.getUuid()+" does not exists.", caller.getRequestedTenantId(), domain);
		return false;
	}

	@Override
	public <Entity extends IGGAPIEntity> List<Entity> getEntities(GGAPIDynamicDomain domain, IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc) {
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Getting entities", caller.getRequestedTenantId(), domain);

		List<Entity> entities = new ArrayList<Entity>();
		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> objects = null;

		Pageable page = null; 
				
		if( pageSize > 0 ) {
			page = PageRequest.of(pageIndex, pageSize);
		} 
			
		GGAPILiteral filterFromCallerInfosAndDomainInfos = getFilterFromCallerInfosAndDomainInfos(caller, domain, filter);
		objects = this.daoRepository.find(domain, page, filterFromCallerInfosAndDomainInfos, sort, geoloc);
	
		objects.forEach(s -> {
			entities.add(this.convertDtoToEntity(s));
		});
	
		return entities;
	}

	@Override
	public <Entity extends IGGAPIEntity> void save(IGGAPICaller caller, Entity entity) throws GGAPIEntityException, GGAPIEngineException {
		GGAPIDynamicDomain domain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
		IGGAPIDTOObject<Entity> object = GGAPIDtoFactory.getOneInstance(caller.getRequestedTenantId(), entity);
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Saving entity with uuid "+object.getUuid(), caller.getRequestedTenantId(), domain);

		this.daoRepository.save( object );	
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Entity extends IGGAPIEntity> Entity update(IGGAPICaller caller, Entity entity) throws GGAPIEntityException, GGAPIEngineException {
		GGAPIDynamicDomain domain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
		
		IGGAPIDTOObject<Entity> object = GGAPIDtoFactory.getOneInstance(caller.getRequestedTenantId(), entity);

		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dto = this.daoRepository.find(domain, null, getFilterFromCallerInfosAndDomainInfos(caller, domain, getUuidFilter(entity.getUuid())), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Updating entity with uuid "+object.getUuid(), caller.getRequestedTenantId(), domain);
			IGGAPIDTOObject<IGGAPIEntity> objectToBeUpdated = (IGGAPIDTOObject<IGGAPIEntity>) dto.get(0);
			
			objectToBeUpdated.update( (IGGAPIDTOObject<IGGAPIEntity>) object );
		
			this.daoRepository.save(objectToBeUpdated);

			return this.convertDtoToEntity(objectToBeUpdated);
		
		} else {
			log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Entity with uuid "+object.getUuid()+" does not exist");
			return null;
		}

	}

	@Override
	public <Entity extends IGGAPIEntity> Entity getOneByUuid(GGAPIDynamicDomain domain, IGGAPICaller caller, String uuid) {
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Looking for object with uuid "+uuid, caller.getRequestedTenantId(), domain);
		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dto = this.daoRepository.find(domain, null, getFilterFromCallerInfosAndDomainInfos(caller, domain, getUuidFilter(uuid)), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Object with uuid "+uuid+" found !", caller.getRequestedTenantId(), domain);
			return this.convertDtoToEntity(dto.get(0));
		}
		
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Object with uuid "+uuid+" not found.", caller.getRequestedTenantId(), domain);
		return null;
	}
	
	@Override
	public <Entity extends IGGAPIEntity> Entity getOneById(GGAPIDynamicDomain domain, IGGAPICaller caller, String id) {
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Looking for object with id "+id, caller.getRequestedTenantId(), domain);
		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dto = this.daoRepository.find(domain, null, getFilterFromCallerInfosAndDomainInfos(caller, domain, getIdFilter(id)), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Object with id "+id+" found !", caller.getRequestedTenantId(), domain);
			return this.convertDtoToEntity(dto.get(0));
		}
		
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Object with id "+id+" not found.", caller.getRequestedTenantId(), domain);
		return null;
	}

	@SuppressWarnings("unchecked")
	private <Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> Entity convertDtoToEntity(IGGAPIDTOObject<? extends IGGAPIEntity> iggapidtoObject) {
		Entity e = (Entity) iggapidtoObject.convert();
		e.setId(iggapidtoObject.getId());
		e.setUuid(iggapidtoObject.getUuid());
		return e;
	}

	@Override
	public <Entity extends IGGAPIEntity> void delete(IGGAPICaller caller, Entity entity) throws GGAPIEntityException, GGAPIEngineException {
		GGAPIDynamicDomain domain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
		IGGAPIDTOObject<Entity> object = GGAPIDtoFactory.getOneInstance( caller.getRequestedTenantId(), entity);
		log.debug("	[domain ["+domain.domain+"]] "+caller.toString()+" Deleting entity with Uuid "+object.getUuid(), caller.getRequestedTenantId(), domain);
		
		this.daoRepository.delete(object);
	}

	@Override
	public void setDao(IGGAPIDAORepository dao) {
		this.daoRepository = dao;
	}

	@Override
	public <Entity extends IGGAPIEntity>  String getTenant(Entity entity) throws GGAPIEngineException {
		GGAPIDynamicDomain domain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
		GGAPICaller caller = new GGAPICaller();
		caller.setSuperTenant(true);
		List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> dto = this.daoRepository.find(domain, null, getFilterFromCallerInfosAndDomainInfos(caller, domain, getUuidFilter(entity.getUuid())), null, null);
		
		if( dto.size() >= 1 ){
			return dto.get(0).getTenantId();
		} else {
			return null;
		}
	}
	
	protected static GGAPILiteral getFilterFromCallerInfosAndDomainInfos(IGGAPICaller caller, GGAPIDynamicDomain domain, GGAPILiteral filter) {
		String requestedTenantId = caller.getRequestedTenantId(); 
		String ownerId = caller.getOwnerId();
		boolean superTenant = caller.isSuperTenant();
		String shared = domain.shared;
		
		GGAPILiteral and = GGAPILiteral.and();
		GGAPILiteral tenantIdFilter = requestedTenantId==null?null:GGAPILiteral.eq("tenantId", requestedTenantId);
		GGAPILiteral shareFieldFilter = GGAPILiteral.eq(shared, requestedTenantId);
		GGAPILiteral visibleFilter = GGAPILiteral.eq("visible", true);
		GGAPILiteral ownerIdFilter = ownerId==null?null:GGAPILiteral.eq("ownerId", ownerId);
		
		if( filter != null ) {
			and.andOperator(filter);
		}

		if( superTenant && (requestedTenantId == null || requestedTenantId.isEmpty()) ){
			
		} else {
			if( !domain.publicEntity && !domain.hiddenable ) {
				
				if( shared != null && !shared.isEmpty() ) {
					if( tenantIdFilter != null ) {
						and.andOperator(shareFieldFilter.orOperator(tenantIdFilter));
					} else {
						and.andOperator(shareFieldFilter);
					}
				} else {
					if( tenantIdFilter != null )
						and.andOperator(tenantIdFilter);
				}
			} else if( !domain.publicEntity && domain.hiddenable ) {
				if( shared != null && !shared.isEmpty() ) {
					GGAPILiteral and__ = visibleFilter.andOperator(shareFieldFilter);
					
					if( tenantIdFilter != null ) {
						GGAPILiteral or = and__.orOperator(tenantIdFilter);
						and.andOperator(or);
					} else {
						and.andOperator(and);
					}
				} else {
					if( tenantIdFilter != null )
						and.andOperator(tenantIdFilter);
				}
			} else if( domain.publicEntity && domain.hiddenable ) {
				and.andOperator(visibleFilter);
				if( tenantIdFilter != null ) {
					and.andOperator(tenantIdFilter);
				}
			} 
			
			if( ownerIdFilter != null && domain.ownedEntity) {
				and.andOperator(ownerIdFilter);
			}
		}
		if( and.getLiterals().size() == 1 ) {
			return and.getLiterals().get(0);
		} else if( and.getLiterals().size() > 1) {
			return and;
		} else {
			return null;
		}
	}
	
	private static GGAPILiteral getUuidFilter(String uuid) {
		return GGAPILiteral.eq("uuid", uuid);
	}
	
	private static GGAPILiteral getIdFilter(String id) {
		return GGAPILiteral.eq("id", id);
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {

	}
}
