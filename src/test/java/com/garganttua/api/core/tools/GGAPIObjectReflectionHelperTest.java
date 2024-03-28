package com.garganttua.api.core.tools;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;

class SuperClass {
	Long superField;
}

class Entity extends SuperClass {
	String field;
}

public class GGAPIObjectReflectionHelperTest {
	
	
	@Test
	public void testGetField() {
		Field fieldField = GGAPIObjectReflectionHelper.getField(Entity.class, "field");
		
		assertNotNull(fieldField);
		
		Field testField = GGAPIObjectReflectionHelper.getField(Entity.class, "test");
		
		assertNull(testField);
		
		Field superFieldField = GGAPIObjectReflectionHelper.getField(Entity.class, "superField");
		
		assertNotNull(superFieldField);
	}
	
	
	

}
