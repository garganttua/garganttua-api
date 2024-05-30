package com.garganttua.api.engine.registries.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.dao.GGAPIDao;
import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.engine.registries.impl.GGAPIDomainsRegistry;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;

@GGAPIEntity(domain = "test")
class TestEntity extends GenericGGAPIEntity {
	
}

@GGAPIDto(db = GGAPIDao.FS, entityClass = TestEntity.class)
class TestDto extends GenericGGAPIDto {
	
}

public class GGAPIDomainsRegistryTest {
	
	@Test
	public void test() throws GGAPIEngineException {
		GGAPIDomainsRegistry reg = new GGAPIDomainsRegistry();
		String[] scanPackages = {"com.garganttua.api.engine.registries.impl"};
		reg.scanPackages = scanPackages;
		
		reg.init();
		
		assertEquals(1, reg.getDomains().size());
	}

}
