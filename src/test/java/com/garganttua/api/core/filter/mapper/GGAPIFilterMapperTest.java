package com.garganttua.api.core.filter.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.tooling.objects.mapper.annotations.GGAPIFieldMappingRule;

@GGAPIEntity(
	domain = "test"
)
class Entity extends GenericGGAPIEntity {
	
	private String mapping;
}
@GGAPIDto(entityClass = Entity.class )
class Dto extends GenericGGAPIDto {
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "mapping")
	private String mappingInDto;
}

public class GGAPIFilterMapperTest {
	
	@Test
	public void testUniqueDtoForEntity() throws GGAPIEntityException, GGAPILiteralMapperException, GGAPIDtoException {
		Entity entity = new Entity();
		entity.setUuid("uuid");
		entity.setId("id");
		
		String[] sp = {"com.garganttua.api.core.filter.mapper"};

		GGAPIDomain domain = GGAPIDomain.fromEntityClass(entity.getClass(), sp);
		
		GGAPILiteral filter = GGAPILiteral.eq("mapping", "toto");
		
		GGAPILiteral expectedFilterForDto = GGAPILiteral.eq("mappingInDto", "toto");
		
		List<Pair<Class<?>, GGAPILiteral>> filters = new GGAPIFilterMapper().map(domain, filter);
		
		assertEquals(1, filters.size());
		assertEquals(expectedFilterForDto, filters.get(0).getValue1());

	}
	
	@Test
	public void testUniqueDtoEntityFieldNotMapped() throws GGAPIEntityException, GGAPILiteralMapperException, GGAPIDtoException {
		Entity entity = new Entity();
		entity.setUuid("uuid");
		entity.setId("id");
		
		String[] sp = {"com.garganttua.api.core.filter.mapper"};

		GGAPIDomain domain = GGAPIDomain.fromEntityClass(entity.getClass(), sp);
		
		GGAPILiteral filter = GGAPILiteral.eq("fieldNotMapped", "toto");
		
		List<Pair<Class<?>, GGAPILiteral>> filters = new GGAPIFilterMapper().map(domain, filter);
		
		assertEquals(0, filters.size());
	}
	
	@Test
	public void testUniqueDtoEntityFieldNotMappedAndOneMapped() throws GGAPIEntityException, GGAPILiteralMapperException, GGAPIDtoException {
		Entity entity = new Entity();
		entity.setUuid("uuid");
		entity.setId("id");
		
		String[] sp = {"com.garganttua.api.core.filter.mapper"};

		GGAPIDomain domain = GGAPIDomain.fromEntityClass(entity.getClass(), sp);
		
		GGAPILiteral filter = GGAPILiteral.eq("fieldNotMapped", "toto");
		GGAPILiteral filter2 = GGAPILiteral.eq("mapping", "toto");
		GGAPILiteral entityFilter = GGAPILiteral.and(filter, filter2);
		
		GGAPILiteral filter3 = GGAPILiteral.eq("mappingInDto", "toto");
		GGAPILiteral expectedFilterForDto = GGAPILiteral.and(filter3);
		
		List<Pair<Class<?>, GGAPILiteral>> filters = new GGAPIFilterMapper().map(domain, entityFilter);
		
		assertEquals(1, filters.size());
		assertEquals(expectedFilterForDto, filters.get(0).getValue1());
	}

}
