package com.garganttua.api.core.entity;

import java.util.Map;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.entity.IEntityDeleteMethod;
import com.garganttua.api.spec.entity.IEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.EntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.EntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.EntityDeleteMethodProvider;
import com.garganttua.api.spec.entity.annotations.EntityEngine;
import com.garganttua.api.spec.entity.annotations.EntityGotFromRepository;
import com.garganttua.api.spec.entity.annotations.EntityId;
import com.garganttua.api.spec.entity.annotations.EntityMandatory;
import com.garganttua.api.spec.entity.annotations.EntityRepository;
import com.garganttua.api.spec.entity.annotations.EntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.EntitySaveMethodProvider;
import com.garganttua.api.spec.entity.annotations.EntitySuperTenant;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.repository.IRepository;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GenericTenantEntity {
	
	@EntityUuid
	@Setter
	@EntityMandatory
	@EntityTenantId
	protected String uuid;
	
	@EntityId
	@Setter
	@EntityMandatory
	@EntityAuthorizeUpdate
	protected String id;
	
	@Setter
	@EntitySuperTenant
	@EntityAuthorizeUpdate(authority = "super-tenant-update")
	protected boolean superTenant;
	
	@EntityGotFromRepository
	private boolean gotFromRepository;

	@EntitySaveMethodProvider
	protected IEntitySaveMethod saveMethod;

	@EntityDeleteMethodProvider
	protected IEntityDeleteMethod deleteMethod;
	
	@EntityRepository
	protected IRepository repository;
	
	@EntityEngine
	protected IEngine engine;

	@EntitySaveMethod
	public Object save(ICaller caller, Map<String, String> parameters) throws CoreException {
		return this.saveMethod.save(caller, parameters, this);
	}

	@EntityDeleteMethod
	public void delete(ICaller caller, Map<String, String> parameters) throws CoreException {
		this.deleteMethod.delete(caller, parameters, this);
	}
}