package com.garganttua.api.core.entity;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityEngine;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityGotFromRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySuperTenant;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPISecurity;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GenericGGAPITenantEntity {
	
	@GGAPIEntityUuid
	@Setter
	@GGAPIEntityMandatory
	@GGAPIEntityTenantId
	protected String uuid;
	
	@GGAPIEntityId
	@Setter
	@GGAPIEntityMandatory
	protected String id;
	
	@Setter
	@GGAPIEntitySuperTenant
	protected boolean superTenant;
	
	@GGAPIEntityGotFromRepository
	private boolean gotFromRepository;

	@GGAPIEntitySaveMethodProvider
	protected IGGAPIEntitySaveMethod<GenericGGAPITenantEntity> saveMethod;

	@GGAPIEntityDeleteMethodProvider
	protected IGGAPIEntityDeleteMethod<GenericGGAPITenantEntity> deleteMethod;
	
	@GGAPIEntityRepository
	protected IGGAPIRepository<Object> repository;
	
	@GGAPIEntityEngine
	protected IGGAPIEngine engine;

	@GGAPIEntitySaveMethod
	public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIException {
		this.saveMethod.save(caller, parameters, this);
	}

	@GGAPIEntityDeleteMethod
	public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIException {
		this.deleteMethod.delete(caller, parameters, this);
	}
}