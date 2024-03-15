package com.garganttua.api.core.dto.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.DtoClassInfos;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.DtoFieldMapping;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.EntityClassInfos;
import com.garganttua.api.core.mapper.annotations.GGAPIDtoFieldMapping;
import com.garganttua.api.core.mapper.annotations.GGAPIDtoObjectMapping;


class Entity extends GenericGGAPIEntity {
	String field;
}

class Dto extends GenericGGAPIDto {
	
	@GGAPIDtoFieldMapping(entityField = "field")
	private String test;
}

class DtoWithFieldWrongType extends GenericGGAPIDto {
	
	@GGAPIDtoFieldMapping(entityField = "field")
	private long test;
}

class DtoWithMethods extends GenericGGAPIDto {
	
	@GGAPIDtoFieldMapping(entityField = "field", fromMethod = "from", toMethod = "to")
	private long test;
	
	long from(String value) {
		return 0;
	}
	
	String to(long value) {
		return "";
	}
}

class DtoWithWrongMethod extends GenericGGAPIDto {
	
	@GGAPIDtoFieldMapping(entityField = "field", fromMethod = "from", toMethod = "to")
	private long test;
	
	long from(String value) {
		return 0;
	}
	
	String to(String value) {
		return "";
	}
}

class DtoWithoutUuidMapping {
	

}

class DtoWithoutIdMapping {
	
	@GGAPIDtoFieldMapping(entityField = "uuid")
	String uuid;
	
}

@GGAPIDtoObjectMapping(fromMethod="from", toMethod="to")
class DtoWitObjectMappingMethod {
	
	@GGAPIDtoFieldMapping(entityField = "uuid")
	String uuid;
	
	void from(Entity entity) {
		
	}
	
	void to(Entity entity) {

	}
	
}

@GGAPIDtoObjectMapping(fromMethod="from", toMethod="to")
class DtoWitObjectMappingMethodButWrongType {
	
	@GGAPIDtoFieldMapping(entityField = "uuid")
	String uuid;
	
	void from(String entity) {
		
	}
	
	void to(Entity entity) {

	}
	
}

public class GGAPIDtoCheckerTest {
	
	private static GGAPIDtoChecker checker;

	@BeforeAll
	public static void init() {
		GGAPIDtoCheckerTest.checker = new GGAPIDtoChecker();
	}

	@Test 
	public void testMappingWithEntityField() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put("test", new DtoFieldMapping("field", "", ""));
		mappings.put("uuid", new DtoFieldMapping("uuid", "", ""));
		mappings.put("id", new DtoFieldMapping("id", "", ""));

		DtoClassInfos result = GGAPIDtoCheckerTest.checker.checkDtoClass(Dto.class, Entity.class, new EntityClassInfos("test",  List.of(Dto.class), "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		DtoClassInfos expectedResult = new DtoClassInfos(mappings);
		
		assertEquals(expectedResult, result);
	}
	
	@Test 
	public void testMappingWithEntityFieldButWrongType() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put("test", new DtoFieldMapping("field", "", ""));
		mappings.put("uuid", new DtoFieldMapping("uuid", "", ""));
		mappings.put("id", new DtoFieldMapping("id", "", ""));

		GGAPIDtoException exception = assertThrows(GGAPIDtoException.class, () -> {
			GGAPIDtoCheckerTest.checker.checkDtoClass(DtoWithFieldWrongType.class, Entity.class, new EntityClassInfos("test", List.of(DtoWithFieldWrongType.class), "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		});
		
		assertEquals("Dto class com.garganttua.api.core.dto.checker.DtoWithFieldWrongType field test must be of type class java.lang.String", exception.getMessage());
		assertEquals(GGAPIDtoException.DTO_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test 
	public void testMappingWithMethods() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put("test", new DtoFieldMapping("field", "from", "to"));
		mappings.put("uuid", new DtoFieldMapping("uuid", "", ""));
		mappings.put("id", new DtoFieldMapping("id", "", ""));

		DtoClassInfos result = GGAPIDtoCheckerTest.checker.checkDtoClass(DtoWithMethods.class, Entity.class, new EntityClassInfos("test",  List.of(DtoWithMethods.class),  "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		DtoClassInfos expectedResult = new DtoClassInfos(mappings);
		
		assertEquals(expectedResult, result);
	}
	
	@Test 
	public void testMappingWithWrongMethod() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put("test", new DtoFieldMapping("field", "from", "to"));
		mappings.put("uuid", new DtoFieldMapping("uuid", "", ""));
		mappings.put("id", new DtoFieldMapping("id", "", ""));

		GGAPIDtoException exception = assertThrows(GGAPIDtoException.class, () -> {
			GGAPIDtoCheckerTest.checker.checkDtoClass(DtoWithWrongMethod.class, Entity.class, new EntityClassInfos("test",  List.of(DtoWithWrongMethod.class), "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		});
		
		assertEquals("Dto class com.garganttua.api.core.dto.checker.DtoWithWrongMethod does not have method class java.lang.String to(long)", exception.getMessage());
		assertEquals(GGAPIDtoException.DTO_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test 
	public void testNoUuidMapping() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put("test", new DtoFieldMapping("field", "from", "to"));
		mappings.put("uuid", new DtoFieldMapping("uuid", "", ""));
		mappings.put("id", new DtoFieldMapping("id", "", ""));

		GGAPIDtoException exception = assertThrows(GGAPIDtoException.class, () -> {
			GGAPIDtoCheckerTest.checker.checkDtoClass(DtoWithoutUuidMapping.class, Entity.class, new EntityClassInfos("test",  List.of(DtoWithoutUuidMapping.class), "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		});
		
		assertEquals("Dto class com.garganttua.api.core.dto.checker.DtoWithoutUuidMapping does not have mapping rule for uuid entity field", exception.getMessage());
		assertEquals(GGAPIDtoException.DTO_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test 
	public void testObjectmapping() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put(GGAPIDtoChecker.OBJECT_MAPPING_RULE, new DtoFieldMapping(null, "from", "to"));

		DtoClassInfos result = GGAPIDtoCheckerTest.checker.checkDtoClass(DtoWitObjectMappingMethod.class, Entity.class, new EntityClassInfos("test",  List.of(DtoWitObjectMappingMethod.class),  "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		DtoClassInfos expectedResult = new DtoClassInfos(mappings);
		
		assertEquals(expectedResult, result);
	}
	
	@Test 
	public void testObjectmappingWrongType() throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, DtoFieldMapping>();
		mappings.put(GGAPIDtoChecker.OBJECT_MAPPING_RULE, new DtoFieldMapping(null, "from", "to"));

		GGAPIDtoException exception = assertThrows(GGAPIDtoException.class, () -> {
			GGAPIDtoCheckerTest.checker.checkDtoClass(DtoWitObjectMappingMethodButWrongType.class, Entity.class, new EntityClassInfos("test",  List.of(DtoWitObjectMappingMethodButWrongType.class), "uuid", "id", null, null, false, false, false, null, null, null, null, null, null, false, false, null, false, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null));
		});
		
		assertEquals("Dto class com.garganttua.api.core.dto.checker.DtoWitObjectMappingMethodButWrongType does not have method from(class com.garganttua.api.core.dto.checker.Entity)", exception.getMessage());
		assertEquals(GGAPIDtoException.DTO_DEFINITION_ERROR, exception.getCode());
	}
	
}
