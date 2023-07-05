package com.garganttua.api.spec;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractGGAPIEntity implements IGGAPIEntity {
	
	@JsonProperty
	private String uuid;
	
	@JsonProperty
	private String id;
	
	public AbstractGGAPIEntity(String uuid, String id) {
		this.uuid = uuid;
		this.id = id;		
	}
	
	public AbstractGGAPIEntity() {
		this.uuid = UUID.randomUUID().toString();	
	}

}
