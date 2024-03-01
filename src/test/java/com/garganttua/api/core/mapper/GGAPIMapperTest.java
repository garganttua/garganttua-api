package com.garganttua.api.core.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.dto.GenericGGAPIDTOObject;
import com.garganttua.api.core.dto.annotations.GGAPIDtoFieldMapping;
import com.garganttua.api.core.dto.annotations.GGAPIDtoObjectMapping;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;

import lombok.Getter;
import lombok.Setter;


@GGAPIEntity(domain = "test", dto = "com.garganttua.api.core.mapper.GenericDto")
class GenericEntity extends GenericGGAPIEntity {
	@Getter
	@Setter
	long longField;
}

@GGAPIEntity(domain = "test", dto = "com.garganttua.api.core.mapper.GenericDtoWithObjectMapping")
class GenericEntityWithObjectMapping extends GenericGGAPIEntity {
	@Getter
	@Setter
	long longField;
}


class GenericDto extends GenericGGAPIDTOObject {
	
	@GGAPIDtoFieldMapping(entityField = "longField", fromMethod = "fromMethod", toMethod = "toMethod")
	String longField;
	
	public GenericDto() {
	}
	
	private String fromMethod(long longField) {
		return String.valueOf(longField);
	}
	
	private long toMethod(String value) {
		return Long.valueOf(this.longField);
	}
}

@GGAPIDtoObjectMapping(fromMethod = "fromMethod", toMethod = "toMethod")
class GenericDtoWithObjectMapping extends GenericGGAPIDTOObject {
	
	@GGAPIDtoFieldMapping(entityField = "longField", fromMethod = "fromMethod", toMethod = "toMethod")
	String longField;
	
	
	private void fromMethod(GenericEntityWithObjectMapping entity) {
		this.id = entity.getId();
		this.uuid = entity.getUuid();
		this.longField = String.valueOf(entity.getLongField());
	}
	
	private void toMethod(GenericEntityWithObjectMapping entity) {

	}
}

public class GGAPIMapperTest {
	
	private static GGAPIMapper mapper;
	

	@BeforeAll
	public static void init() {
		GGAPIMapperTest.mapper = new GGAPIMapper();
	}
	
	@Test
	public void testToDtoGenericMapping() throws GGAPIMapperException {
			
		GenericEntity entity = new GenericEntity();
		entity.setId("id");
		entity.setUuid("uuid");
		entity.setLongField(12);

		GenericDto dto = (GenericDto) GGAPIMapperTest.mapper.mapToDto(entity);
		
		assertNotNull(dto);
		assertEquals(entity.getId(), dto.getId());
		assertEquals(entity.getUuid(), dto.getUuid());
		assertEquals("12", dto.longField);
	}

	@Test
	public void testToDtoGenericMappingAtObjectLevel() throws GGAPIMapperException {
			
		GenericEntityWithObjectMapping entity = new GenericEntityWithObjectMapping();
		entity.setId("id");
		entity.setUuid("uuid");
		entity.setLongField(12);

		GenericDtoWithObjectMapping dto = (GenericDtoWithObjectMapping) GGAPIMapperTest.mapper.mapToDto(entity);
		
		assertNotNull(dto);
		assertEquals(entity.getId(), dto.getId());
		assertEquals(entity.getUuid(), dto.getUuid());
		assertEquals("12", dto.longField);
	}
	
	@Test
	public void testMappingAutoDetectWay() throws GGAPIMapperException {
			
		GenericEntityWithObjectMapping entity = new GenericEntityWithObjectMapping();
		entity.setId("id");
		entity.setUuid("uuid");
		entity.setLongField(12);

		GenericDtoWithObjectMapping dto = (GenericDtoWithObjectMapping) GGAPIMapperTest.mapper.map(entity);
		
		assertNotNull(dto);
		assertEquals(entity.getId(), dto.getId());
		assertEquals(entity.getUuid(), dto.getUuid());
		assertEquals("12", dto.longField);
	}
	
	@Test 
	public void testMappingTime() throws GGAPIMapperException {
		GenericEntity entity = new GenericEntity();
		entity.setId("id");
		entity.setUuid("uuid");
		entity.setLongField(12);

		long totalTime = 0;

		for(int i = 0; i < 10000 ; i++) {
			Date start = new Date();
			GenericDto dto = (GenericDto) GGAPIMapperTest.mapper.map(entity);
			Date end = new Date();
			totalTime += end.getTime() - start.getTime();
		}
		
		System.out.println("Mapping total time "+totalTime);
		System.out.println("Average mapping time "+totalTime/10000);
		
		assertTrue( totalTime < 1000?true:false );
		assertTrue( (totalTime/10000) < 10?true:false );
	}
	
}
