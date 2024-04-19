package com.garganttua.api.core.objects.fields.accessors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.GGAPIObjectAddressException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;

import lombok.Getter;

public class GGAPIObjectFieldSetterTest {
	
	@Getter
	public static class ObjectTest {
		
		private ObjectTest inner;
		
		private long l;
		
		private String s;
		
		private float f;
		
		private int i;
		
		private Set<ObjectTest> innersInSet;
		
		private List<ObjectTest> innersInList;
		
		private ObjectTest[] innersInArray;
		
		private Map<ObjectTest,ObjectTest> innersInMap;
	}

	@Test
	public void testSetLong() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("l");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);
 
		ObjectTest object = (ObjectTest) setter.setValue(1L);
		
		assertNotNull(object);
		assertEquals(1L, object.getL());
		
	}
	
	@Test
	public void testSetString() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "s"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("s");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);
 
		ObjectTest object = (ObjectTest) setter.setValue("test");
		
		assertNotNull(object);
		assertEquals("test", object.getS());
		
	}
	
	@Test
	public void testSetValueInInner() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.l");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);
 
		ObjectTest object = (ObjectTest) setter.setValue(1L);
		
		assertNotNull(object);
		assertEquals(1L, object.getInner().getL());
	}
	
	@Test
	public void testSetValueInInnerWithDepth6() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException , GGAPIObjectAddressException{
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.inner.inner.inner.inner.l", false);
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);
 
		ObjectTest object = (ObjectTest) setter.setValue(1L);
		
		assertNotNull(object);
		assertEquals(1L, object.getInner().getInner().getInner().getInner().getInner().getL());
	}
	
	@Test
	public void testSetValuesInList() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersInList.l");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);

		ObjectTest object = (ObjectTest) setter.setValue(List.of(1L, 2L, 3L));
		
		assertNotNull(object);
		assertEquals(3, object.getInnersInList().size());
		assertEquals(1L, object.getInnersInList().get(0).getL());
		assertEquals(2L, object.getInnersInList().get(1).getL());
		assertEquals(3L, object.getInnersInList().get(2).getL());
	}
	
	@Test
	public void testSetValuesInListDepth2() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersInList.innersInList.l", false);
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);

		ObjectTest object = (ObjectTest) setter.setValue(List.of(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L)));
		
		assertNotNull(object);
		assertEquals(2, object.getInnersInList().size());
		assertEquals(1L, object.getInnersInList().get(0).getInnersInList().get(0).getL());
		assertEquals(2L, object.getInnersInList().get(0).getInnersInList().get(1).getL());
		assertEquals(3L, object.getInnersInList().get(0).getInnersInList().get(2).getL());
		assertEquals(1L, object.getInnersInList().get(1).getInnersInList().get(0).getL());
		assertEquals(2L, object.getInnersInList().get(1).getInnersInList().get(1).getL());
		assertEquals(3L, object.getInnersInList().get(1).getInnersInList().get(2).getL());
	}
	
	@Test
	public void testSetValuesInSetDepth2() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInSet"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersInSet.innersInList.l");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);

		ObjectTest object = (ObjectTest) setter.setValue(List.of(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L)));
		
		assertNotNull(object);
		assertEquals(2, object.getInnersInSet().size());
		object.getInnersInSet().forEach(objectTest -> {
			assertEquals(1L, objectTest.getInnersInList().get(0).getL());
			assertEquals(2L, objectTest.getInnersInList().get(1).getL());
			assertEquals(3L, objectTest.getInnersInList().get(2).getL());
		});
	}
	
	@Test
	public void testSetValuesInArrayDepth2() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInArray"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersInArray.innersInList.l");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);
		List<Object> values = List.of(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L));
		
		ObjectTest object = (ObjectTest) setter.setValue(values);
		
		assertNotNull(object);
		assertEquals(2, object.getInnersInArray().length);
		for( ObjectTest objectTest: object.getInnersInArray() ) {
			assertEquals(1L, objectTest.getInnersInList().get(0).getL());
			assertEquals(2L, objectTest.getInnersInList().get(1).getL());
			assertEquals(3L, objectTest.getInnersInList().get(2).getL());
		}
	}
	
	@Test
	public void testSetValuesInMapDepth2() throws GGAPIObjectQueryException, NoSuchMethodException, SecurityException, GGAPIObjectAddressException {
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInMap"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersInMap.#key.innersInList.l");
		
		GGAPIObjectFieldSetter setter = new GGAPIObjectFieldSetter(ObjectTest.class, fieldInfos, address);
		List<Object> values = List.of(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L));
		
		ObjectTest object = (ObjectTest) setter.setValue(values);
		
		assertNotNull(object);
		assertEquals(2, object.getInnersInMap().size());
		object.getInnersInMap().keySet().forEach(objectTest -> {
			assertEquals(1L, objectTest.getInnersInList().get(0).getL());
			assertEquals(2L, objectTest.getInnersInList().get(1).getL());
			assertEquals(3L, objectTest.getInnersInList().get(2).getL());
		});
	}
}


