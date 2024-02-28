package com.garganttua.api.core.entity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.core.entity.annotations.GGAPIEntityId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.core.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

import lombok.Getter;

@Getter
public class AbstractGGAPIEntity {
	
	@JsonProperty
	@GGAPIEntityUuid
	protected String uuid;
	
	@JsonProperty
	@GGAPIEntityId
	protected String id;
	
	@JsonIgnore
	private boolean gotFromRepository;

	@JsonIgnore
	@GGAPIEntitySaveMethodProvider
	transient protected IGGAPIEntitySaveMethod saveMethod;

	@JsonIgnore
	@GGAPIEntityDeleteMethodProvider
	transient protected IGGAPIEntityDeleteMethod deleteMethod;
	
	@JsonIgnore
	@GGAPIEntityRepository
	transient protected IGGAPIRepository repository;

	public AbstractGGAPIEntity(String uuid, String id) {
		this.uuid = uuid;
		this.id = id;
	}
	
	public AbstractGGAPIEntity() {
		this.uuid = UUID.randomUUID().toString();	
	}

	@GGAPIEntitySaveMethod
	public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
		this.saveMethod.save(GGAPIDynamicDomain.fromEntityClass(this.getClass()), this.repository, caller, security, parameters, this);
	}

	@GGAPIEntityDeleteMethod
	public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
		this.deleteMethod.delete(GGAPIDynamicDomain.fromEntityClass(this.getClass()), this.repository, caller, parameters, this);
	}

}