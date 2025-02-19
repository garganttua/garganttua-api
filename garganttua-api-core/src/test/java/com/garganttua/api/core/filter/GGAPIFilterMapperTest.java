package com.garganttua.api.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.NoArgsConstructor;

@GGAPIEntity(
	domain = "test", interfaces = { "gg:test" }
)
@NoArgsConstructor
class Entity extends GenericGGAPIEntity {
	
	protected Entity(String uuid, String id) {
		super(uuid, id);
	}

	private String mapping;
}
@GGAPIDto(entityClass = Entity.class )
class Dto extends GenericGGAPIDto {
	
	@GGFieldMappingRule(sourceFieldAddress = "mapping")
	private String mappingInDto;
}

public class GGAPIFilterMapperTest {
	
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void testUniqueDtoForEntity() throws GGAPIException {
		Entity entity = new Entity("uuid", "id");

		GGAPIDomain domain = GGAPIDomain.fromEntityClass(entity.getClass(), List.of("com.garganttua.api.core.filter"));
		
		System.out.println(domain);
		
		GGAPILiteral filter = GGAPILiteral.eq("mapping", "toto");
		
		GGAPILiteral expectedFilterForDto = GGAPILiteral.eq("mappingInDto", "toto");
		
		List<Pair<Class<?>, IGGAPIFilter>> filters = new GGAPIFilterMapper().map(domain, filter);
		
		assertEquals(1, filters.size());
		assertEquals(expectedFilterForDto, filters.get(0).getValue1());
	}
	
	@Test
	public void testUniqueDtoEntityFieldNotMapped() throws GGAPIException {
		Entity entity = new Entity("uuid", "id");

		GGAPIDomain domain = GGAPIDomain.fromEntityClass(entity.getClass(), List.of("com.garganttua.api.core.filter"));
		
		GGAPILiteral filter = GGAPILiteral.eq("fieldNotMapped", "toto");
		
		List<Pair<Class<?>, IGGAPIFilter>> filters = new GGAPIFilterMapper().map(domain, filter);
		
		assertEquals(0, filters.size());
	}
	
	@Test
	public void testUniqueDtoEntityFieldNotMappedAndOneMapped() throws GGAPIException {
		Entity entity = new Entity("uuid", "id");
		
		GGAPIDomain domain = GGAPIDomain.fromEntityClass(entity.getClass(), List.of("com.garganttua.api.core.filter"));
		
		GGAPILiteral filter = GGAPILiteral.eq("fieldNotMapped", "toto");
		GGAPILiteral filter2 = GGAPILiteral.eq("mapping", "toto");
		GGAPILiteral entityFilter = GGAPILiteral.and(filter, filter2);
		
		GGAPILiteral filter3 = GGAPILiteral.eq("mappingInDto", "toto");
		GGAPILiteral expectedFilterForDto = GGAPILiteral.and(filter3);
		
		List<Pair<Class<?>, IGGAPIFilter>> filters = new GGAPIFilterMapper().map(domain, entityFilter);
		
		assertEquals(1, filters.size());
		assertEquals(expectedFilterForDto, filters.get(0).getValue1());
	}

}
