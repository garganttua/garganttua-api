package com.garganttua.api.core.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.mapper.fieldFinder.GGAPIFieldFinder;
import com.garganttua.api.core.mapper.fieldFinder.GGAPIFieldFinderException;

class TestClass extends ParentClass {
	private String field;
	
	private InnerClass inner;
	
	private Map<String, InnerClass> map1;
	
	private Map<InnerClass, String> map2;
	
	private Set<InnerClass> set;
	
	private Collection<InnerClass> collection;
	
	private List<InnerClass> list;
	
}

class ParentClass {
	private boolean parentField;
}

class InnerClass {
	private long innerField;
}


public class GGAPIFieldFinderTest {
	
	@Test
    void testFindFieldInObject() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "field");
		Pair<Field, Class<?>> field2 = finder.findField(TestClass.class, "map2");
		
		assertNotNull(field);
		assertNotNull(field2);
		
		GGAPIFieldFinderException exception = assertThrows(GGAPIFieldFinderException.class, () -> {
			finder.findField(TestClass.class, "error");
		});
    }
	
	@Test
    void testFindFieldInSuperObject() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "parentField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInInnerObject() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "parentField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInList() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "list.innerField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInSet() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "set.innerField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInCollection() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "collection.innerField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInMapValue() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "map1.value.innerField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInMapKey() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		Pair<Field, Class<?>> field = finder.findField(TestClass.class, "map2.key.innerField");
		
		assertNotNull(field);
    }
	
	@Test
    void testFindFieldInMapwithoutKeyOrValueInAddress() throws GGAPIFieldFinderException {
		GGAPIFieldFinder finder = new GGAPIFieldFinder();
		
		GGAPIFieldFinderException exception = assertThrows(GGAPIFieldFinderException.class, () -> {
			finder.findField(TestClass.class, "map2.innerField");
		});
	
		assertEquals("Field map2 is a map, so address must indicate key or value", exception.getMessage());
    }
}