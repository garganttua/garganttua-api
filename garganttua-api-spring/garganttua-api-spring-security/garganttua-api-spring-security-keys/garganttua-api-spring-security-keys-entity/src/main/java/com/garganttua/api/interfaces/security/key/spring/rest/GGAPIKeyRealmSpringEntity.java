package com.garganttua.api.interfaces.security.key.spring.rest;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.garganttua.api.core.security.key.GGAPIKeyRealm;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;

import lombok.NoArgsConstructor;

@GGAPIEntity(domain = GGAPIKeyRealm.domain, interfaces = { "gg:SpringRestInterface" })
@JsonIgnoreProperties(value = { "gotFromRepository","saveMethod","deleteMethod", "repository", "save", "delete", "engine", "keyForCiphering", "keyForUnciphering", "name", "cipheringKey", "uncipheringKey" })
@NoArgsConstructor
public class GGAPIKeyRealmSpringEntity extends GGAPIKeyRealm {
	
	public GGAPIKeyRealmSpringEntity(String keyRealmName, String algorithm, Date expiration) {
		this.id = keyRealmName;
		this.algorithm = algorithm;
		this.expiration = expiration;
	}

}
