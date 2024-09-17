package com.garganttua.api.security.authorizations.spring.jwt;

import org.springframework.data.mongodb.core.mapping.Document;

import com.garganttua.api.security.authorizations.daos.mongo.spring.GGAPISpringSecurityAuthorizationDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

@GGAPIDto(entityClass = GGAPISpringJWTAuthorization.class, db = "gg:SpringMongoDao")
@Document(collection = "authorizations")
public class GGAPISpringSecurityJWTAuthorizationDto extends GGAPISpringSecurityAuthorizationDto {

	@GGFieldMappingRule(sourceFieldAddress = "jwtAlgo")
	private String jwtAlgo;
	
}
