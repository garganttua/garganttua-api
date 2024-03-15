/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.dto.tools.GGAPIDtoHelper;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.entity.updater.GGAPIEntityUpdaterException;
import com.garganttua.api.core.entity.updater.IGGAPIEntityUpdater;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.filter.mapper.GGAPILiteralMapperException;
import com.garganttua.api.core.filter.mapper.IGGAPIFilterMapper;
import com.garganttua.api.core.mapper.GGAPIMapperException;
import com.garganttua.api.core.mapper.IGGAPIMapper;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dao.GGAPIDaoException;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIUniqueRepository implements IGGAPIRepository<Object> {

	private IGGAPIDAORepository<Object> daoRepository;
	
	@Setter
	private IGGAPIFilterMapper filterMapper;
	
	@Setter
	private IGGAPIMapper entityMapper;
	
	@Setter
	private IGGAPIEntityUpdater<Object> entityUpdater;
	
	@Setter
	private GGAPIDomain domain;
	
	@Override
    public long getCount(IGGAPICaller caller, GGAPILiteral filter) throws GGAPIRepositoryException {
    	log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Get Total Count, Filter {}", caller.getRequestedTenantId(), domain, filter);
    	try {
	    	long totalCount = 0;
	    	
	    	GGAPILiteral filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, filter);
	    	List<Pair<Class<?>, GGAPILiteral>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
    	
			totalCount = this.daoRepository.count(dtoFilters.get(0).getValue1());
			return totalCount;
		} catch (GGAPIDaoException | GGAPILiteralMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
    	
    }

	@Override
	public boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException {
		log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Checking if entity with uuid {} exists.", caller.getRequestedTenantId(), domain);

		try {
			GGAPILiteral filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getUuidFilter(this.domain.entity.getValue1().uuidFieldName(), uuid));
			List<Pair<Class<?>, GGAPILiteral>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			
			List<?> dto= this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+"Entity with uuid "+uuid+" exists.");
				return true;
			} 
			log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Entity with uuid "+uuid+" does not exists.", caller.getRequestedTenantId(), domain);
			return false;
		} catch (GGAPIDaoException | GGAPILiteralMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
		
	}
	
	@Override
	public boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		try {
			if( GGAPIEntityHelper.getUuid(entity) == null || GGAPIEntityHelper.getUuid(entity).isEmpty() ) {
				return false;
			}
			return this.doesExist(caller, GGAPIEntityHelper.getUuid(entity));
		} catch (GGAPIEntityException e) {
			throw new GGAPIRepositoryException(e);
		}
	}

	@Override
	public List<Object> getEntities(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort) throws GGAPIRepositoryException {
		log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Getting entities", caller.getRequestedTenantId(), domain);

		List<Object> entities = new ArrayList<Object>();
		List<?> objects = null;

		Pageable page = null; 
				
		if( pageSize > 0 ) {
			page = PageRequest.of(pageIndex, pageSize);
		} 
		
		try {
			GGAPILiteral filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, filter);
			List<Pair<Class<?>, GGAPILiteral>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			objects = this.daoRepository.find(page, dtoFilters.get(0).getValue1(), sort);
			for( Object object: objects) {
				entities.add(this.entityMapper.map(object, this.domain.entity.getValue0()));
			}
		} catch (GGAPIDaoException | GGAPILiteralMapperException | GGAPIMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
	
		return entities;
	}

	@Override
	public void save(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		try {
			log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Saving entity with uuid "+GGAPIEntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
			this.daoRepository.save( this.entityMapper.map(entity, this.domain.dtos.get(0).getValue0()) );
		} catch (GGAPIEntityException | GGAPIDaoException | GGAPIMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
	}

	@Override
	public Object update(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		
		Object storedObject;
		try {
			storedObject = this.getOneByUuid(caller, GGAPIEntityHelper.getUuid(entity));
		
			if( storedObject != null ){
				log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Updating entity with uuid "+GGAPIEntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
				
				this.entityUpdater.update(caller, storedObject, entity);
				this.daoRepository.save(storedObject);
				return this.entityMapper.map(storedObject, this.domain.entity.getValue0());
			
			} else {
				log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Entity with uuid "+GGAPIEntityHelper.getUuid(entity)+" does not exist");
				return null;
			}
		} catch (GGAPIEntityException | GGAPIEntityUpdaterException | GGAPIDaoException | GGAPIMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
	}

	@Override
	public Object getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException {
		log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Looking for object with uuid "+uuid, caller.getRequestedTenantId(), this.domain);
		GGAPILiteral filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getUuidFilter(this.domain.entity.getValue1().uuidFieldName(), uuid));

		try {
			List<Pair<Class<?>, GGAPILiteral>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<Object> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Object with uuid "+uuid+" found !", caller.getRequestedTenantId(), this.domain);
				return this.entityMapper.map(dto.get(0), this.domain.entity.getValue0());
			}
			
			log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Object with uuid "+uuid+" not found.", caller.getRequestedTenantId(), this.domain);
		} catch (GGAPIDaoException | GGAPIMapperException | GGAPILiteralMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
		return null;
	}
	
	@Override
	public Object getOneById(IGGAPICaller caller, String id) throws GGAPIRepositoryException {
		log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Looking for object with id "+id, caller.getRequestedTenantId(), this.domain);
		GGAPILiteral filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getIdFilter(this.domain.entity.getValue1().idFieldName(), id));
		try {
			List<Pair<Class<?>, GGAPILiteral>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<?> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			
			if( dto.size() >= 1 ){
				log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Object with id "+id+" found !", caller.getRequestedTenantId(), this.domain);
				return this.entityMapper.map(dto.get(0), this.domain.entity.getValue0());
			}
		} catch (GGAPILiteralMapperException | GGAPIDaoException | GGAPIMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
		
		log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Object with id "+id+" not found.", caller.getRequestedTenantId(), this.domain);
		return null;
	}

	@Override
	public void delete(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		try {
			log.debug("	[domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Deleting entity with Uuid "+GGAPIEntityHelper.getUuid(entity), caller.getRequestedTenantId(), domain);
			this.daoRepository.delete(this.entityMapper.map(entity, this.domain.dtos.get(0).getValue0()));
		} catch (GGAPIEntityException | GGAPIDaoException | GGAPIMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
	}

	@Override
	public String getTenant(Object entity) throws GGAPIRepositoryException {
		GGAPICaller caller = new GGAPICaller();
		caller.setSuperTenant(true);
		
		try {
			GGAPILiteral filterUp = GGAPIRepositoryFilterTools.getFilterFromCallerInfosAndDomainInfos(caller, domain, GGAPIRepositoryFilterTools.getUuidFilter(this.domain.entity.getValue1().uuidFieldName(), GGAPIEntityHelper.getUuid(entity)));
			List<Pair<Class<?>, GGAPILiteral>> dtoFilters = this.filterMapper.map(this.domain, filterUp);
			List<?> dto = this.daoRepository.find(null, dtoFilters.get(0).getValue1(), null);
			if( dto.size() >= 1 ){
				return GGAPIDtoHelper.getTenantId(dto.get(0));
			} else {
				return null;
			}
		} catch (GGAPIDaoException | GGAPIDtoException | GGAPIEntityException | GGAPILiteralMapperException e) {
			throw new GGAPIRepositoryException(e);
		}
		
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDaos(List<Pair<Class<?>, IGGAPIDAORepository<?>>> daos) {
		this.daoRepository = (IGGAPIDAORepository<Object>) daos.get(0).getValue1();
	}
}
