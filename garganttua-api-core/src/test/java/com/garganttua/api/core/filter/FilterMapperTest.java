package com.garganttua.api.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.dto.GenericDto;
import com.garganttua.api.core.entity.GenericEntity;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dto.annotations.Dto;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.NoArgsConstructor;

@Entity(
	domain = "test", interfaces = { "gg:test" }
)
@NoArgsConstructor
class EntityTest extends GenericEntity {
	
	protected EntityTest(String uuid, String id) {
		super(uuid, id);
	}

	private String mapping;
}

@Dto(entityClass = EntityTest.class )
class DtoTest extends GenericDto {
	
	@GGFieldMappingRule(sourceFieldAddress = "mapping")
	private String mappingInDto;
}

public class FilterMapperTest {
	
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void testUniqueDtoForEntity() throws CoreException {
		EntityTest entity = new EntityTest("uuid", "id");

		Domain domain = Domain.fromEntityClass(entity.getClass(), List.of("com.garganttua.api.core.filter"));
		
		System.out.println(domain);
		
		Literal filter = Literal.eq("mapping", "toto");
		
		Literal expectedFilterForDto = Literal.eq("mappingInDto", "toto");
		
		List<Pair<Class<?>, IFilter>> filters = new FilterMapper().map(domain, filter);
		
		assertEquals(1, filters.size());
		assertEquals(expectedFilterForDto, filters.get(0).getValue1());
	}
	
	@Test
	public void testUniqueDtoEntityFieldNotMapped() throws CoreException {
		EntityTest entity = new EntityTest("uuid", "id");

		Domain domain = Domain.fromEntityClass(entity.getClass(), List.of("com.garganttua.api.core.filter"));
		
		Literal filter = Literal.eq("fieldNotMapped", "toto");
		
		List<Pair<Class<?>, IFilter>> filters = new FilterMapper().map(domain, filter);
		
		assertEquals(0, filters.size());
	}
	
	@Test
	public void testUniqueDtoEntityFieldNotMappedAndOneMapped() throws CoreException {
		EntityTest entity = new EntityTest("uuid", "id");
		
		Domain domain = Domain.fromEntityClass(entity.getClass(), List.of("com.garganttua.api.core.filter"));
		
		Literal filter = Literal.eq("fieldNotMapped", "toto");
		Literal filter2 = Literal.eq("mapping", "toto");
		Literal entityFilter = Literal.and(filter, filter2);
		
		Literal filter3 = Literal.eq("mappingInDto", "toto");
		Literal expectedFilterForDto = Literal.and(filter3);
		
		List<Pair<Class<?>, IFilter>> filters = new FilterMapper().map(domain, entityFilter);
		
		assertEquals(1, filters.size());
		assertEquals(expectedFilterForDto, filters.get(0).getValue1());
	}

}
