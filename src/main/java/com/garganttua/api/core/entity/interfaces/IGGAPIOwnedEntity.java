package com.garganttua.api.core.entity.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface IGGAPIOwnedEntity extends IGGAPIEntity {
	
	@JsonProperty(value = "ownerId")
	public String getOwnerId();
	
	public void setOwnerId(String ownerId);

}
