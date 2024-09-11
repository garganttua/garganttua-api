package com.garganttua.api.interfaces.security.key.spring.rest;

import com.garganttua.api.security.keys.domain.GGAPIKeyRealmEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;

@GGAPIEntity(domain = GGAPIKeyRealmEntity.domain, interfaces = { "gg:SpringRestInterface" })
public class GGAPIKeyRealmSpringRestEntity extends GGAPIKeyRealmEntity {

}
