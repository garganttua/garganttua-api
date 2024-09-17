package com.garganttua.api.daos.security.key.spring.mongo;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringEntity;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.objects.mapper.GGMapper;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class GGAPIKeyRealmSpringMongoDtoMappingTest {
	
	@Test
	public void test() throws GGAPIException, GGReflectionException, GGMapperException {
		GGAPIKeyRealmSpringEntity entity = new GGAPIKeyRealmSpringEntity("test", "HmacSHA512-1024", null);
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		
		IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
		
		objectQuery.invoke(entity, infos.beforeCreateMethodAddress(), null, null);
		
		System.out.println("************************************* MApping *********************************");
		
		GGMapper mapper = new GGMapper();
		
		GGAPIKeyRealmSpringMongoDto dto = mapper.map(entity, GGAPIKeyRealmSpringMongoDto.class);

		System.out.println();
	}

}
