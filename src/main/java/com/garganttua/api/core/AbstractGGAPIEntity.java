package com.garganttua.api.core;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractGGAPIEntity implements IGGAPIEntity {
	
	@JsonProperty
	protected String uuid;
	
	@JsonProperty
	protected String id;
	
	@JsonIgnore
	private boolean gotFromRepository;

	@JsonIgnore
	transient protected IGGAPIEntitySaveMethod saveMethod;

	@JsonIgnore
	transient protected IGGAPIEntityDeleteMethod deleteMethod;
	
	@JsonIgnore
	transient protected IGGAPIRepository repository;

	public AbstractGGAPIEntity(String uuid, String id) {
		this.uuid = uuid;
		this.id = id;
	}
	
	public AbstractGGAPIEntity() {
		this.uuid = UUID.randomUUID().toString();	
	}

	@Override
	public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
		this.saveMethod.save(GGAPIDynamicDomain.fromEntityClass(this.getClass()), this.repository, caller, security, parameters, this);
	}

	@Override
	public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
		this.deleteMethod.delete(GGAPIDynamicDomain.fromEntityClass(this.getClass()), this.repository, caller, parameters, this);
	}

}