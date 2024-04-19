package com.garganttua.api.core.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.mapper.annotations.GGAPIFieldMappingRule;
import com.garganttua.api.core.mapper.annotations.GGAPIObjectMappingRule;

import lombok.Getter;
import lombok.Setter;


@GGAPIEntity(domain = "test")
class GenericEntity extends GenericGGAPIEntity {
	@Getter
	@Setter
	long longField;
}

@GGAPIEntity(domain = "test")
class GenericEntityWithObjectMapping extends GenericGGAPIEntity {
	@Getter
	@Setter
	long longField;
}


@GGAPIEntity(domain = "test")
class GenericEntityFromTwoDtos extends GenericGGAPIEntity {
	@Getter
	@Setter
	long longField;
	
	@Getter
	@Setter
	long otherField;
}


class GenericDto extends GenericGGAPIDto {
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "longField", fromSourceMethod = "fromMethod", toSourceMethod = "toMethod")
	String longField;
	
	public GenericDto() {
	}
	
	private String fromMethod(long longField) {
		return String.valueOf(longField);
	}
	
	private long toMethod(String value) {
		return Long.valueOf(value);
	}
}

class SecondGenericDto extends GenericGGAPIDto {
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "otherField", fromSourceMethod = "fromMethod", toSourceMethod = "toMethod")
	String longField;
	
	public SecondGenericDto() {
	}
	
	private String fromMethod(long longField) {
		return String.valueOf(longField);
	}
	
	private long toMethod(String value) {
		return Long.valueOf(value);
	}
}

@GGAPIObjectMappingRule(fromSourceMethod = "fromMethod", toSourceMethod = "toMethod")
class GenericDtoWithObjectMapping extends GenericGGAPIDto {
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "longField", fromSourceMethod = "fromMethod", toSourceMethod = "toMethod")
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

	@Test
	public void testRegularFieldMapping() throws GGAPIMapperException {
		
		GenericGGAPIEntity entity = new GenericGGAPIEntity();
		entity.setUuid("uuid");
		entity.setId("id");
		
		GenericGGAPIDto dest = new GGAPIMapper().configure(GGAPIMapperConfigurationItem.FAIL_ON_ERROR, false).map(entity, GenericGGAPIDto.class);
		
		assertNotNull(dest);
		assertEquals("uuid", dest.getUuid());
		assertEquals("id", dest.getId());
		
	}
	
	@Test
	public void testReverseFieldMapping() throws GGAPIMapperException {
		
		GenericGGAPIDto dto = new GenericGGAPIDto();
		dto.setUuid("uuid");
		dto.setId("id");
		
		GenericGGAPIEntity dest = new GGAPIMapper().configure(GGAPIMapperConfigurationItem.FAIL_ON_ERROR, false).map(dto, GenericGGAPIEntity.class);
		
		assertNotNull(dest);
		assertEquals("uuid", dest.getUuid());
		assertEquals("id", dest.getId());
		
	}
	
	@Test
	public void testMappingConfigurationNotFailOnError() {
		GenericGGAPIEntity entity = new GenericGGAPIEntity();
		entity.setUuid("uuid");
		entity.setId("id");
		
		assertThrows(GGAPIMapperException.class, () -> {
			new GGAPIMapper().configure(GGAPIMapperConfigurationItem.FAIL_ON_ERROR, true).map(entity, GenericGGAPIDto.class);
		});
	}
}
