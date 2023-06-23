package com.garganttua.api.spec;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public abstract class AbstractGGAPIEntity implements IGGAPIEntity {
	
	@JsonProperty
	private String uuid;
	
	@JsonProperty
	private String id;

}
