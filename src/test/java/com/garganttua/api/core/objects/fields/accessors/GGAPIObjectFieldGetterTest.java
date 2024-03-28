package com.garganttua.api.core.objects.fields.accessors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;

import lombok.AllArgsConstructor;

public class GGAPIObjectFieldGetterTest {
	
	@AllArgsConstructor
	class ObjectTest {
		
		private ObjectTest inner;
		
		private long l;
		
		private String s;
		
		private float f;
		
		private int i;
		
		private List<ObjectTest> innersInList;
		
		private ObjectTest[] innersInArray;
		
		private Map<ObjectTest,ObjectTest> innersInMap;
	}
	
	private ObjectTest createNestedObject(int depth) {
        if (depth <= 0) {
            return null;
        }

        ObjectTest inner = createNestedObject(depth - 1);
        List<ObjectTest> innerList = new ArrayList<>();
        innerList.add(createNestedObject(depth - 1));

        ObjectTest[] innerArray = new ObjectTest[1];
        innerArray[0] = createNestedObject(depth - 1);

        Map<ObjectTest, ObjectTest> innerMap = new HashMap<>();
        innerMap.put(createNestedObject(depth - 1), createNestedObject(depth - 1));

        return new ObjectTest(inner, depth, "Depth-" + depth, depth, depth, innerList, innerArray, innerMap);
    }
	
	@Test
	public void testGetValueOfField() throws GGAPIObjectQueryException {
		
		ObjectTest o = this.createNestedObject(1);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "l"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("l");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address );
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1L, value);
	}
	
	@Test
	public void testGetValueOfFieldOfInner() throws GGAPIObjectQueryException {
		
		ObjectTest o = this.createNestedObject(2);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "f"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.f");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1F, value);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetValueOfFieldOfInnerInList() throws GGAPIObjectQueryException {
		
		ObjectTest o = this.createNestedObject(2);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.f");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertTrue(List.class.isAssignableFrom(value.getClass()));
		assertEquals(1, ((List<Object>) value).get(0));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetValueOfFieldOfInnerInKeyOfMap() throws GGAPIObjectQueryException {
		
		ObjectTest o = this.createNestedObject(2);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInMap"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersMap.#key.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertTrue(List.class.isAssignableFrom(value.getClass()));
		assertTrue(((List<Object>) value).contains(1));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetValueOfFieldOfInnerInValueOfMap() throws GGAPIObjectQueryException {
		
		ObjectTest o = this.createNestedObject(2);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInMap"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("innersMap.#value.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertTrue(List.class.isAssignableFrom(value.getClass()));
		assertTrue(((List<Object>) value).contains(1));
	}
	
	@Test
	public void testGetValueOfFieldOfInnerInInner() throws GGAPIObjectQueryException {

		ObjectTest o = this.createNestedObject(3);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1, value);
	}
	
	@Test
	public void testGetValueOfFieldDepth6() throws GGAPIObjectQueryException {

		ObjectTest o = this.createNestedObject(6);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.inner.inner.inner.inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1, value);
		
		List<Object> fieldInfos2 = new ArrayList<Object>();
		fieldInfos2.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos2.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos2.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos2.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address2 = new GGAPIObjectAddress("inner.inner.inner.i");
		GGAPIObjectFieldGetter getter2 = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos2, address2);
		
		Object value2 = getter2.getValue(o);
		assertNotNull(value2);
		assertEquals(3, value2);
	}
	
	@Test
	public void testGetValueOfFieldInInnerMapDepth6() throws GGAPIObjectQueryException {

		ObjectTest o = this.createNestedObject(6);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.innerInMap.#key.inner.inner.inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1, value);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetValueOfFieldInListDepth6() throws GGAPIObjectQueryException {

		ObjectTest o = this.createNestedObject(6);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.inner.innersInList.inner.inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1, ((List<Object>) value).get(0));
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetValueOfFieldInMapDepth6() throws GGAPIObjectQueryException {

		ObjectTest o = this.createNestedObject(6);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInMap"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.inner.inner.innersInMap.#key.inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1, ((List<Object>) value).get(0));
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetValueDepth6ComplexInner() throws GGAPIObjectQueryException {

		ObjectTest o = this.createNestedObject(6);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInList"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInArray"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "innersInMap"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.innersInList.innersInArray.innersInMap.#value.inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNotNull(value);
		assertEquals(1, ((List<Object>) ((List<Object>) ((List<Object>) value).get(0)).get(0)).get(0));
		
	}

	@Test
	public void testGetNullValue() throws GGAPIObjectQueryException {

		ObjectTest o = new ObjectTest(null, 0, null, 0, 0, null, null, null);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNull(value);
	}
	

	@Test
	public void testGetNullValueFromNullInner() throws GGAPIObjectQueryException {

		ObjectTest o = new ObjectTest(null, 0, null, 0, 0, null, null, null);
		
		List<Object> fieldInfos = new ArrayList<Object>();
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "inner"));
		fieldInfos.add(GGAPIObjectReflectionHelper.getField(ObjectTest.class, "i"));
		
		GGAPIObjectAddress address = new GGAPIObjectAddress("inner.i");
		GGAPIObjectFieldGetter getter = new GGAPIObjectFieldGetter(ObjectTest.class, fieldInfos, address);
		
		Object value = getter.getValue(o);
		assertNull(value);
	}

}
