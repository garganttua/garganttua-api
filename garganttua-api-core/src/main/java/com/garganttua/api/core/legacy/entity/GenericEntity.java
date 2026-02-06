package com.garganttua.api.core.legacy.entity;

import java.util.Map;
import java.util.Objects;

import com.garganttua.core.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IEngine;
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
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.repository.IRepository;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class GenericEntity {
	
	@EntityUuid
	@Setter
	@EntityMandatory 
	protected String uuid;
	
	@EntityId
	@Setter
	@EntityMandatory
	protected String id;
	
	@Setter
	@EntityTenantId
	@EntityAuthorizeUpdate(authority = "entity-tenant-update")
	protected String tenantId;
	
	protected GenericEntity(String uuid) {
		this(uuid, uuid);
	}
	
	protected GenericEntity(String tenantId, String uuid, String id) {
		this(uuid, id);
		this.tenantId = tenantId;
	}
	
	protected GenericEntity(String uuid, String id) {
		this.uuid = uuid;
		this.id = id;
	}
	
	protected GenericEntity() {
	}
	
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GenericEntity that = (GenericEntity) o;

		return Objects.equals(uuid, that.uuid) &&
				Objects.equals(id, that.id) &&
			   Objects.equals(tenantId, that.tenantId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), uuid, id, tenantId);
	}
}