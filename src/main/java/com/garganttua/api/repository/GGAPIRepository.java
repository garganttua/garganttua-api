/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPIDomainable;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableMongoRepositories
public class GGAPIRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIRepository<Entity, Dto> {
	
	public GGAPIRepository(IGGAPIDomain<Entity, Dto> domain) {
		super(domain);
	}

	@Inject
	protected IGGAPIDAORepository<Entity, Dto> daoRepository;
	
	@Override
    public long getCount(String tenantId, GGAPILiteral filter) {
    	log.info("[Tenant {}] [Domain {}] Get Total Count, Filter {}", tenantId, this.domain, filter);
    	long totalCount = 0;
    	
    	totalCount = this.daoRepository.countByTenantId(tenantId, filter);
    	
    	return totalCount;
    }

	@Override
	public boolean doesExists(String tenantId, String uuid) {
 
		log.info("[Tenant {}] [Domain {}] Checking if entity with uuid {} exists.", tenantId, this.domain);
		
		if( this.daoRepository.findOneByUuidAndTenantId(uuid, tenantId) != null ){
			log.info("Entity with uuid "+uuid+" exists.");
			return true;
		} 
		log.info("[Tenant {}] [Domain {}] Entity with uuid "+uuid+" does not exists.", tenantId, this.domain);
		return false;
	}
	
	@Override
	public boolean doesExists(String tenantId, Entity entity) {
 
		Dto object = this.dtoFactory.newInstance(tenantId, entity);
		
		log.info("[Tenant {}] [Domain {}] Checking if entity with uuid "+object.getUuid()+" exists.", tenantId, this.domain);
		
		if( this.daoRepository.findOneByUuidAndTenantId(object.getUuid(), object.getTenantId()) != null ){
			log.info("[Tenant "+tenantId+"] Entity with uuid "+object.getUuid()+" exists.");
			return true;
		} 
		log.info("[Tenant {}] [Domain {}] Entity with uuid "+object.getUuid()+" does not exists.", tenantId, this.domain);
		return false;
	}

	@Override
	public List<Entity> getEntities(String tenantId, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort) {
		log.info("[Tenant {}] [Domain {}] Getting entities", tenantId, this.domain);

		List<Entity> entities = new ArrayList<Entity>();
		List<Dto> objects = null;

		Pageable page = null; 
				
		if( pageSize > 0 ) {
			page = PageRequest.of(pageIndex, pageSize);
		} 
			
		objects = this.daoRepository.findByTenantId(tenantId, page, filter, sort);
	
		objects.forEach(s ->{
			entities.add((Entity) s.convert());
		});
	
		return entities;
	}

	@Override
	public void save(String tenantId, Entity entity) {
		Dto object = this.dtoFactory.newInstance(tenantId, entity);
		log.info("[Tenant {}] [Domain {}] Saving entity with uuid "+object.getUuid()+" exists.", tenantId, this.domain);

		this.daoRepository.save( object );
		
	}

	@Override
	public Entity update(String tenantId, Entity entity) {
		
		Dto object = this.dtoFactory.newInstance(tenantId, entity);

		Dto objectToBeUpdated = this.daoRepository.findOneByUuidAndTenantId(object.getUuid(), object.getTenantId());
		log.info("[Tenant {}] [Domain {}] Updating entity with uuid "+object.getUuid()+" exists.", tenantId, this.domain);
		
		if( objectToBeUpdated != null ){
			
			objectToBeUpdated.update(object);
		
			this.daoRepository.save(objectToBeUpdated);
			
			return (Entity) object.convert();
		
		} else {
			return null;
		}

	}

	@Override
	public Entity getOneByUuid(String tenantId, String uuid) {
		log.info("[Tenant {}] [Domain {}] Looking for object with uuid "+uuid, tenantId, this.domain);
		Dto object = this.daoRepository.findOneByUuidAndTenantId(uuid, tenantId);
		
		if( object != null ){
			log.info("[Tenant {}] [Domain {}] Object with uuid "+uuid+" found !", tenantId, this.domain);
			return (Entity) object.convert();
		}
		
		log.info("[Tenant {}] [Domain {}] Object with uuid "+uuid+" not found.", tenantId, this.domain);
		return null;
	}
	
	@Override
	public Entity getOneById(String tenantId, String id) {
		log.info("[Tenant {}] [Domain {}] Looking for object with id "+id, tenantId, this.domain);
		Dto object = this.daoRepository.findOneByIdAndTenantId(id, tenantId);
		
		if( object != null ){
			log.info("[Tenant {}] [Domain {}] Object with id "+id+" found !", tenantId, this.domain);
			return (Entity) object.convert();
		}
		
		log.info("[Tenant {}] [Domain {}] Object with id "+id+" not found.", tenantId, this.domain);
		return null;
	}

	@Override
	public void delete(String tenantId, Entity entity) {
		Dto object = this.dtoFactory.newInstance(tenantId, entity);

		log.info("[Tenant {}] [Domain {}] Deleting entity with Uuid "+object.getUuid(), tenantId, this.domain);
		
		this.daoRepository.delete(object);
	}

	@Override
	public void setDao(IGGAPIDAORepository<Entity, Dto> dao) {
		this.daoRepository = dao;
	}

	@Override
	public String getTenant(Entity entity) {
		Dto object = this.daoRepository.findOneByUuidAndTenantId(entity.getUuid(), this.daoRepository.getMagicTenantId());
		
		return object.getTenantId();
	}

	@Override
	public boolean doesExist(String tenantId, String fieldName, String fieldValue) {
		
		log.info("[Tenant {}] [Domain {}] Checking if entity with field {} valued at {} exists", tenantId, this.domain, fieldName, fieldValue);
		
		/*
		 * WARNING : This method takes in account that fields of entities and dto are exactly the same, which may not be the case
		 */
		
		String fiterString = "{\"name\":\"$field\", \"value\":\""+fieldName+"\",\"literals\":[{\"name\":\"$eq\",\"value\":\""+fieldValue+"\"}]}";
		
		ObjectMapper mapper = new ObjectMapper();
		GGAPILiteral filter = null;
		try {
			filter = mapper.readValue(fiterString, GGAPILiteral.class);
		} catch (JsonProcessingException e) {
			
		}
		
		List<Dto> entities = this.daoRepository.findByTenantId(tenantId, null, filter, null);
		
		return entities.size()>0?true:false;
	}


}
