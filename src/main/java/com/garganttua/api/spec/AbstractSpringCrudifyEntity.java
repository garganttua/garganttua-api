package com.garganttua.api.spec;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public abstract class AbstractSpringCrudifyEntity implements ISpringCrudifyEntity {
	
	@JsonProperty
	private String uuid;
	
	@JsonProperty
	private String id;

}
