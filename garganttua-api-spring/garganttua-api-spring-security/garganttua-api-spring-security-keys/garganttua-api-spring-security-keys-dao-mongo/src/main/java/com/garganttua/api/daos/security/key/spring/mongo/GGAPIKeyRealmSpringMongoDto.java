package com.garganttua.api.daos.security.key.spring.mongo;

import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringRestEntity;
import com.garganttua.api.security.keys.domain.GGAPIKeyRealmDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;

@GGAPIDto(entityClass = GGAPIKeyRealmSpringRestEntity.class, db = "gg:SpringMongoDao")
public class GGAPIKeyRealmSpringMongoDto extends GGAPIKeyRealmDto {

}
