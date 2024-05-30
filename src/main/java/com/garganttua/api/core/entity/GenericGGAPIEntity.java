package com.garganttua.api.core.entity;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityGotFromRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPISecurity;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GenericGGAPIEntity {
	
	@JsonProperty
	@GGAPIEntityUuid
	@Setter
	@GGAPIEntityMandatory
	protected String uuid;
	
	@JsonProperty
	@GGAPIEntityId
	@Setter
	@GGAPIEntityMandatory
	protected String id;
	
	@JsonIgnore
	@GGAPIEntityGotFromRepository
	private boolean gotFromRepository;

	@JsonIgnore
	@GGAPIEntitySaveMethodProvider
	protected IGGAPIEntitySaveMethod<GenericGGAPIEntity> saveMethod;

	@JsonIgnore
	@GGAPIEntityDeleteMethodProvider
	protected IGGAPIEntityDeleteMethod<GenericGGAPIEntity> deleteMethod;
	
	@JsonIgnore
	@GGAPIEntityRepository
	protected IGGAPIRepository<Object> repository;

	@GGAPIEntitySaveMethod
	public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
		this.saveMethod.save(caller, parameters, this);
	}

	@GGAPIEntityDeleteMethod
	public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
		this.deleteMethod.delete(caller, parameters, this);
	}

}