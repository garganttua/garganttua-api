/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.dto.tools.DtoHelper;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.filter.FilterMapper;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.core.filter.IFilterMapper;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.objects.mapper.IGGMapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleRepository implements IRepository {

	private IDao<Object> daoRepository;
	
	@Setter
	private IFilterMapper filterMapper = new FilterMapper();
	
	@Setter
	private IGGMapper entityMapper = DefaultMapper.mapper();

	@Setter
	protected IDomain domain;

	private IEngine engine;
	
	@Override
    public long getCount(ICaller caller, IFilter filter) throws CoreException {
    	log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Get Total Count, Filter {}", caller.getRequestedTenantId(), domain, filter);
    	long totalCount = 0;
		
    	IFilter filterUp = RepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, (Literal) filter);
		List<Pair<Class<?>, IFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
  	
		totalCount = this.daoRepository.count(dtoFilters.get(0).getValue1());
		return totalCount;
    	
    }

	@Override
	public boolean doesExist(ICaller caller, String uuid) throws CoreException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Checking if entity with uuid {} exists.", caller.getRequestedTenantId(), domain);

		IFilter filterUp = RepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, RepositoryFilterTools.getUuidFilter(this.domain.getUuidFieldAddress().toString(), uuid));
		List<Pair<Class<?>, IFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
		
		List<?> dto= this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
		
		if( dto.size() >= 1 ){
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+"Entity with uuid "+uuid+" exists.");
			return true;
		} 
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Entity with uuid "+uuid+" does not exists.", caller.getRequestedTenantId(), domain);
		return false;
		
	}
	
	@Override
	public boolean doesExist(ICaller caller, Object entity) throws CoreException {
		try {
			if( EntityHelper.getUuid(entity) == null || EntityHelper.getUuid(entity).isEmpty() ) {
				return false;
			}
			return this.doesExist(caller, EntityHelper.getUuid(entity));
		} catch (EntityException e) {
			throw new EngineException(e);
		}
	}

	@Override
	public List<Object> getEntities(ICaller caller, IPageable pageable, IFilter filter, ISort sort) throws CoreException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Getting entities", caller.getRequestedTenantId(), domain);

		List<Object> entities = new ArrayList<Object>();
		List<?> objects = null;
		
		IFilter filterUp = RepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, (Literal) filter);
		List<Pair<Class<?>, IFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
		objects = this.daoRepository.find(pageable, dtoFilters.get(0).getValue1(), sort);
		try {
			if( objects != null ) {
				for( Object object: objects) {
					entities.add(this.entityMapper.map(object, this.domain.getEntityClass()));
				}
			}
		} catch (GGMapperException e) {
			throw new EngineException(e);
		}
		return entities;
	}

	@Override
	public void save(ICaller caller, Object entity) throws CoreException {
		try {
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Saving entity with uuid "+EntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
			this.daoRepository.save( this.entityMapper.map(entity, this.domain.getDtos().get(0).getValue0()) );
		} catch (GGMapperException e) {
			throw new EngineException(e);
		}
	}

	@Override
	public Optional<Object> getOneByUuid(ICaller caller, String uuid) throws CoreException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Looking for object with uuid "+uuid, caller.getRequestedTenantId(), this.domain);
		IFilter filterUp = RepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, RepositoryFilterTools.getUuidFilter(this.domain.getUuidFieldAddress().toString(), uuid));

		try {
			List<Pair<Class<?>, IFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<Object> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with uuid "+uuid+" found !", caller.getRequestedTenantId(), this.domain);
				return Optional.ofNullable(this.entityMapper.map(dto.get(0), this.domain.getEntityClass()));
			}
			
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with uuid "+uuid+" not found.", caller.getRequestedTenantId(), this.domain);
		} catch (GGMapperException e) {
			throw new EngineException(e);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<Object> getOneById(ICaller caller, String id) throws CoreException {
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Looking for object with id "+id, caller.getRequestedTenantId(), this.domain);
		IFilter filterUp = RepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, RepositoryFilterTools.getIdFilter(this.domain.getIdFieldAddress().toString(), id));
		try {
			List<Pair<Class<?>, IFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<?> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with id "+id+" found !", caller.getRequestedTenantId(), this.domain);
				return Optional.ofNullable(this.entityMapper.map(dto.get(0), this.domain.getEntityClass()));
			}
		} catch (GGMapperException e) {
			throw new EngineException(e);
		}
		
		log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Object with id "+id+" not found.", caller.getRequestedTenantId(), this.domain);
		return Optional.empty();
	}

	@Override
	public void delete(ICaller caller, Object entity) throws CoreException {
		try {
			log.debug("	[domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Deleting entity with Uuid "+EntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
			this.daoRepository.delete(this.entityMapper.map(entity, this.domain.getDtos().get(0).getValue0()));
		} catch (GGMapperException e) {
			throw new EngineException(e);
		}
	}

	@Override
	public String getTenant(Object entity) throws CoreException {		
		IFilter filterUp = RepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(Caller.createSuperCaller(), domain, RepositoryFilterTools.getUuidFilter(this.domain.getUuidFieldAddress().toString(), EntityHelper.getUuid(entity)));
		List<Pair<Class<?>, IFilter>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
		List<?> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
		if( dto.size() >= 1 ){
			return DtoHelper.getTenantId(dto.get(0));
		} else {
			return null;
		}
	}

	@Override
	public void setEngine(IEngine engine) {
		this.engine = engine;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDaos(List<Pair<Class<?>, IDao<?>>> daos) {
		this.daoRepository = (IDao<Object>) daos.get(0).getValue1();
	}

}
