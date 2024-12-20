/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.repository;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.dto.tools.GGAPIDtoHelper;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPIFilterMapper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.filter.IGGAPIFilterMapper;
import com.garganttua.api.core.mapper.GGAPIDefaultMapper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.sort.IGGAPISort;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.objects.mapper.IGGMapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISimpleRepository implements IGGAPIRepository {

	private IGGAPIDao<Object> daoRepository;
	
	@Setter
	private IGGAPIFilterMapper filterMapper = new GGAPIFilterMapper();
	
	@Setter
	private IGGMapper entityMapper = GGAPIDefaultMapper.mapper();

	@Setter
	protected IGGAPIDomain domain;

	private IGGAPIEngine engine;
	
	@Override
    public long getCount(IGGAPICaller caller, IGGAPIFilter filter) throws GGAPIException {
    	log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Get Total Count, Filter {}", caller.getRequestedTenantId(), domain, filter);
    	long totalCount = 0;
		
    	IGGAPIFilter filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, (GGAPILiteral) filter);
		List<Pair<Class<?>, IGGAPIFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
  	
		totalCount = this.daoRepository.count(dtoFilters.get(0).getValue1());
		return totalCount;
    	
    }

	@Override
	public boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Checking if entity with uuid {} exists.", caller.getRequestedTenantId(), domain);

		IGGAPIFilter filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getUuidFilter(this.domain.getUuidFieldAddress().toString(), uuid));
		List<Pair<Class<?>, IGGAPIFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
		
		List<?> dto= this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+"Entity with uuid "+uuid+" exists.");
			return true;
		} 
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Entity with uuid "+uuid+" does not exists.", caller.getRequestedTenantId(), domain);
		return false;
		
	}
	
	@Override
	public boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIException {
		try {
			if( GGAPIEntityHelper.getUuid(entity) == null || GGAPIEntityHelper.getUuid(entity).isEmpty() ) {
				return false;
			}
			return this.doesExist(caller, GGAPIEntityHelper.getUuid(entity));
		} catch (GGAPIEntityException e) {
			throw new GGAPIEngineException(e);
		}
	}

	@Override
	public List<Object> getEntities(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort) throws GGAPIException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Getting entities", caller.getRequestedTenantId(), domain);

		List<Object> entities = new ArrayList<Object>();
		List<?> objects = null;
		
		IGGAPIFilter filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, (GGAPILiteral) filter);
		List<Pair<Class<?>, IGGAPIFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
		objects = this.daoRepository.find(pageable, dtoFilters.get(0).getValue1(), sort);
		try {
			if( objects != null ) {
				for( Object object: objects) {
					entities.add(this.entityMapper.map(object, this.domain.getEntityClass()));
				}
			}
		} catch (GGMapperException e) {
			throw new GGAPIEngineException(e);
		}
		return entities;
	}

	@Override
	public void save(IGGAPICaller caller, Object entity) throws GGAPIException {
		try {
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Saving entity with uuid "+GGAPIEntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
			this.daoRepository.save( this.entityMapper.map(entity, this.domain.getDtos().get(0).getValue0()) );
		} catch (GGMapperException e) {
			throw new GGAPIEngineException(e);
		}
	}

	@Override
	public Object getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Looking for object with uuid "+uuid, caller.getRequestedTenantId(), this.domain);
		IGGAPIFilter filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getUuidFilter(this.domain.getUuidFieldAddress().toString(), uuid));

		try {
			List<Pair<Class<?>, IGGAPIFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<Object> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with uuid "+uuid+" found !", caller.getRequestedTenantId(), this.domain);
				return this.entityMapper.map(dto.get(0), this.domain.getEntityClass());
			}
			
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with uuid "+uuid+" not found.", caller.getRequestedTenantId(), this.domain);
		} catch (GGMapperException e) {
			throw new GGAPIEngineException(e);
		}
		return null;
	}
	
	@Override
	public Object getOneById(IGGAPICaller caller, String id) throws GGAPIException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Looking for object with id "+id, caller.getRequestedTenantId(), this.domain);
		IGGAPIFilter filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getIdFilter(this.domain.getIdFieldAddress().toString(), id));
		try {
			List<Pair<Class<?>, IGGAPIFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<?> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with id "+id+" found !", caller.getRequestedTenantId(), this.domain);
				return this.entityMapper.map(dto.get(0), this.domain.getEntityClass());
			}
		} catch (GGMapperException e) {
			throw new GGAPIEngineException(e);
		}
		
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with id "+id+" not found.", caller.getRequestedTenantId(), this.domain);
		return null;
	}

	@Override
	public void delete(IGGAPICaller caller, Object entity) throws GGAPIException {
		try {
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Deleting entity with Uuid "+GGAPIEntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
			this.daoRepository.delete(this.entityMapper.map(entity, this.domain.getDtos().get(0).getValue0()));
		} catch (GGMapperException e) {
			throw new GGAPIEngineException(e);
		}
	}

	@Override
	public String getTenant(Object entity) throws GGAPIException {		
		IGGAPIFilter filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(GGAPICaller.createSuperCaller(), domain, GGAPIRepositoryFilterTools.getUuidFilter(this.domain.getUuidFieldAddress().toString(), GGAPIEntityHelper.getUuid(entity)));
		List<Pair<Class<?>, IGGAPIFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
		List<?> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
		if( dto.size() >= 1 ){
			return GGAPIDtoHelper.getTenantId(dto.get(0));
		} else {
			return null;
		}
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.engine = engine;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDaos(List<Pair<Class<?>, IGGAPIDao<?>>> daos) {
		this.daoRepository = (IGGAPIDao<Object>) daos.get(0).getValue1();
	}

}
