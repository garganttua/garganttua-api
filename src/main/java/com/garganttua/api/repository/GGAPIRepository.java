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
import com.garganttua.api.core.GGAPIDomainable;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableMongoRepositories
public class GGAPIRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIRepository<Entity, Dto> {

	protected IGGAPIDAORepository<Entity, Dto> daoRepository;
	private IGGAPIEngine engine;

	@Override
    public long getCount(IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc) {
    	log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Get Total Count, Filter {}", caller.getRequestedTenantId(), this.domain, filter);
    	long totalCount = 0;
    	
    	totalCount = this.daoRepository.count(getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, filter), geoloc);
    	
    	return totalCount;
    }

	@Override
	public boolean doesExist(IGGAPICaller caller, String uuid) {
 
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Checking if entity with uuid {} exists.", caller.getRequestedTenantId(), this.domain);
		
		List<Dto> dto = this.daoRepository.find(null, getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, getUuidFilter(uuid)), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+"Entity with uuid "+uuid+" exists.");
			return true;
		} 
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with uuid "+uuid+" does not exists.", caller.getRequestedTenantId(), this.domain);
		return false;
	}
	
	@Override
	public boolean doesExist(IGGAPICaller caller, Entity entity) {
 
		Dto object = this.dtoFactory.newInstance(caller.getRequestedTenantId(), entity);
		
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Checking if entity with uuid "+object.getUuid()+" exists.", caller.getRequestedTenantId(), this.domain);
		
		List<Dto> dto = this.daoRepository.find(null, getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, getUuidFilter(entity.getUuid())), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with uuid "+object.getUuid()+" exists.");
			return true;
		} 
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with uuid "+object.getUuid()+" does not exists.", caller.getRequestedTenantId(), this.domain);
		return false;
	}

	@Override
	public List<Entity> getEntities(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc) {
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Getting entities", caller.getRequestedTenantId(), this.domain);

		List<Entity> entities = new ArrayList<Entity>();
		List<Dto> objects = null;

		Pageable page = null; 
				
		if( pageSize > 0 ) {
			page = PageRequest.of(pageIndex, pageSize);
		} 
			
		GGAPILiteral filterFromCallerInfosAndDomainInfos = getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, filter);
		objects = this.daoRepository.find(page, filterFromCallerInfosAndDomainInfos, sort, geoloc);
	
		objects.forEach(s -> {
			entities.add(this.convertDtoToEntity(s));
		});
	
		return entities;
	}

	@Override
	public void save(IGGAPICaller caller, Entity entity) {
		Dto object = this.dtoFactory.newInstance(caller.getRequestedTenantId(), entity);
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Saving entity with uuid "+object.getUuid(), caller.getRequestedTenantId(), this.domain);

		this.daoRepository.save( object );
		
	}

	@Override
	public Entity update(IGGAPICaller caller, Entity entity) {
		
		Dto object = this.dtoFactory.newInstance(caller.getRequestedTenantId(), entity);

		List<Dto> dto = this.daoRepository.find(null, getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, getUuidFilter(entity.getUuid())), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Updating entity with uuid "+object.getUuid(), caller.getRequestedTenantId(), this.domain);
			Dto objectToBeUpdated = dto.get(0);
			
			objectToBeUpdated.update(object);
		
			this.daoRepository.save(objectToBeUpdated);

			return this.convertDtoToEntity(objectToBeUpdated);
		
		} else {
			log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with uuid "+object.getUuid()+" does not exist");
			return null;
		}

	}

	@Override
	public Entity getOneByUuid(IGGAPICaller caller, String uuid) {
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Looking for object with uuid "+uuid, caller.getRequestedTenantId(), this.domain);
		List<Dto> dto = this.daoRepository.find(null, getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, getUuidFilter(uuid)), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Object with uuid "+uuid+" found !", caller.getRequestedTenantId(), this.domain);
			return this.convertDtoToEntity(dto.get(0));
		}
		
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Object with uuid "+uuid+" not found.", caller.getRequestedTenantId(), this.domain);
		return null;
	}
	
	@Override
	public Entity getOneById(IGGAPICaller caller, String id) {
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Looking for object with id "+id, caller.getRequestedTenantId(), this.domain);
		List<Dto> dto = this.daoRepository.find(null, getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, getIdFilter(id)), null, null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Object with id "+id+" found !", caller.getRequestedTenantId(), this.domain);
			return this.convertDtoToEntity(dto.get(0));
		}
		
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Object with id "+id+" not found.", caller.getRequestedTenantId(), this.domain);
		return null;
	}

	private Entity convertDtoToEntity(Dto object) {
		Entity e = (Entity) object.convert();
		e.setId(object.getId());
		e.setUuid(object.getUuid());
		return e;
	}

	@Override
	public void delete(IGGAPICaller caller, Entity entity) {
		Dto object = this.dtoFactory.newInstance(caller.getRequestedTenantId(), entity);
		log.debug("	[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Deleting entity with Uuid "+object.getUuid(), caller.getRequestedTenantId(), this.domain);
		
		this.daoRepository.delete(object);
	}

	@Override
	public void setDao(IGGAPIDAORepository<Entity, Dto> dao) {
		this.daoRepository = dao;
	}

	@Override
	public String getTenant(Entity entity) {
		GGAPICaller caller = new GGAPICaller();
		caller.setSuperTenant(true);
		List<Dto> dto = this.daoRepository.find(null, getFilterFromCallerInfosAndDomainInfos(caller, this.dynamicDomain, getUuidFilter(entity.getUuid())), null, null);
		
		if( dto.size() >= 1 ){
			return dto.get(0).getTenantId();
		} else {
			return null;
		}
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.engine = engine;
	}
	
	protected static GGAPILiteral getFilterFromCallerInfosAndDomainInfos(IGGAPICaller caller, GGAPIDynamicDomain domain, GGAPILiteral filter) {
		String requestedTenantId = caller.getRequestedTenantId(); 
		boolean superTenant = caller.isSuperTenant();
		String shared = domain.shared();
		
		List<GGAPILiteral> andLiterals = new ArrayList<GGAPILiteral>();
		if( filter != null ) {
			andLiterals.add(filter);
		}
		
		GGAPILiteral tenantIdFilter = requestedTenantId==null?null:GGAPILiteral.getFilterForTestingFieldEquality("tenantId", requestedTenantId);

		if( superTenant && (requestedTenantId == null || requestedTenantId.isEmpty()) ){
			
		} else {
			if( !domain.publicEntity() && !domain.hiddenable() ) {
				
				if( shared != null && !shared.isEmpty() ) {
					GGAPILiteral shareFieldFilter = GGAPILiteral.getFilterForTestingFieldEquality(shared, requestedTenantId);
					if( tenantIdFilter != null ) {
						List<GGAPILiteral> orList = new ArrayList<GGAPILiteral>();
						orList.add(tenantIdFilter);
						orList.add(shareFieldFilter);
						GGAPILiteral or = new GGAPILiteral(GGAPILiteral.OPERATOR_OR, null, orList );
						andLiterals.add(or);
					} else {
						andLiterals.add(shareFieldFilter);
					}
				} else {
					if( tenantIdFilter != null )
						andLiterals.add(tenantIdFilter);
				}
			} else if( !domain.publicEntity() && domain.hiddenable() ) {
				if( shared != null && !shared.isEmpty() ) {
					GGAPILiteral shareFieldFilter = GGAPILiteral.getFilterForTestingFieldEquality(shared, requestedTenantId);
					GGAPILiteral visibleFilter = GGAPILiteral.getFilterForTestingFieldEquality("visible", true);
					List<GGAPILiteral> andList = new ArrayList<GGAPILiteral>();
					andList.add(shareFieldFilter);
					andList.add(visibleFilter);
					GGAPILiteral and = new GGAPILiteral(GGAPILiteral.OPERATOR_AND, null, andList);
					
					if( tenantIdFilter != null ) {
						List<GGAPILiteral> orList = new ArrayList<GGAPILiteral>();
						orList.add(tenantIdFilter);
						orList.add(and);
						GGAPILiteral or = new GGAPILiteral(GGAPILiteral.OPERATOR_OR, null, orList);
						andLiterals.add(or);
					} else {
						andLiterals.add(and);
					}
				} else {
					if( tenantIdFilter != null )
						andLiterals.add(tenantIdFilter);
				}
			} else if( domain.publicEntity() && domain.hiddenable() ) {
				GGAPILiteral visibleFilter = GGAPILiteral.getFilterForTestingFieldEquality("visible", true);
				andLiterals.add(visibleFilter);
			} 
		}

		if( andLiterals.size() > 1) 
			return new GGAPILiteral(GGAPILiteral.OPERATOR_AND, null, andLiterals);
		else if( andLiterals.size() == 1 )
			return andLiterals.get(0);
		else 
			return null;
	}
	
	private static GGAPILiteral getUuidFilter(String uuid) {
		return GGAPILiteral.getFilterForTestingFieldEquality("uuid", uuid);
	}
	
	private static GGAPILiteral getIdFilter(String id) {
		return GGAPILiteral.getFilterForTestingFieldEquality("id", id);
	}

}
