package com.garganttua.api.interfaces.security.key.spring.rest;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.garganttua.api.security.keys.domain.GGAPIKeyRealmEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;

import lombok.NoArgsConstructor;

@GGAPIEntity(domain = GGAPIKeyRealmEntity.domain, interfaces = { "gg:SpringRestInterface" })
@JsonIgnoreProperties(value = { "gotFromRepository","saveMethod","deleteMethod", "repository", "save", "delete", "engine", "keyForCiphering", "keyForUnciphering", "name" })
@NoArgsConstructor
public class GGAPIKeyRealmSpringEntity extends GGAPIKeyRealmEntity {

	public GGAPIKeyRealmSpringEntity(String keyRealmName, String algorithm, Date expiration) {
		this.id = keyRealmName;
		this.algorithm = algorithm; 
		this.expiration = expiration;
	}
}
