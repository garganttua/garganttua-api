package com.garganttua.api.core.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.mapper.annotations.GGAPIFieldMappingRule;
import com.garganttua.api.core.mapper.annotations.GGAPIObjectMappingRule;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRule;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRuleException;
import com.garganttua.api.core.mapper.rules.GGAPIMappingRules;

class Parent {
	@GGAPIFieldMappingRule(sourceFieldAddress = "parent")
	private String parent;
}

class Inner {
	@GGAPIFieldMappingRule(sourceFieldAddress = "inner")
	private String inner;
}

@GGAPIObjectMappingRule(fromSourceMethod = "from", toSourceMethod = "to")
class Inner2 {
	@GGAPIFieldMappingRule(sourceFieldAddress = "inner")
	private String inner;
	
	public void from() {
		
	}
	public void to() {
		
	}
}

class Destination extends Parent {
	@GGAPIFieldMappingRule(sourceFieldAddress = "field")
	private String field;
	
	private Inner inner;
	
	private List<Inner> list;
	
	private Map<String, Inner> map1;
	
	private Map<Inner, String> map2;
	
	private Set<Inner> set;
	
	private Collection<Inner> collection;
}

@GGAPIObjectMappingRule(fromSourceMethod = "from", toSourceMethod = "to")
class Destination2 extends Parent {
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "field")
	private String field;
	
	private List<Inner2> list;
	
	public void from() {
		
	}
	public void to() {
		
	}

}

class GGAPIMappingRulesTest {
	
	@Test
	void testmappingRuleOnField() throws GGAPIMappingRuleException {
		
		List<GGAPIMappingRule> rules = GGAPIMappingRules.parse(Destination.class);
		
		assertEquals(8, rules.size());
		
		assertEquals("field", rules.get(0).sourceFieldAddress());
		assertNull(rules.get(0).fromSourceMethod());
		assertEquals(String.class, rules.get(0).destinationField().getType());
		assertEquals( Destination.class, rules.get(0).destinationClass());
		
		assertEquals("inner", rules.get(1).sourceFieldAddress());
		assertEquals("inner.inner", rules.get(1).destinationFieldAddress());
		assertNull(rules.get(1).fromSourceMethod());
		assertEquals(String.class, rules.get(1).destinationField().getType());
		assertEquals(Inner.class, rules.get(1).destinationClass());
		
		assertEquals("inner", rules.get(2).sourceFieldAddress());
		assertEquals("list.inner", rules.get(2).destinationFieldAddress());
		assertNull(rules.get(2).fromSourceMethod());
		assertEquals(String.class, rules.get(2).destinationField().getType());
		assertEquals(Inner.class, rules.get(2).destinationClass());
		
		assertEquals("inner", rules.get(3).sourceFieldAddress());
		assertEquals("map1.value.inner", rules.get(3).destinationFieldAddress());
		assertNull(rules.get(3).fromSourceMethod());
		assertEquals(String.class, rules.get(3).destinationField().getType());
		assertEquals(Inner.class, rules.get(3).destinationClass());
		
		assertEquals("inner", rules.get(4).sourceFieldAddress());
		assertEquals("map2.key.inner", rules.get(4).destinationFieldAddress());
		assertNull(rules.get(4).fromSourceMethod());
		assertEquals(String.class, rules.get(4).destinationField().getType());
		assertEquals(Inner.class, rules.get(4).destinationClass());
		
		assertEquals("inner", rules.get(5).sourceFieldAddress());
		assertEquals("set.inner", rules.get(5).destinationFieldAddress());
		assertNull(rules.get(5).fromSourceMethod());
		assertEquals(String.class, rules.get(5).destinationField().getType());
		assertEquals(Inner.class, rules.get(5).destinationClass());
		
		assertEquals("inner", rules.get(6).sourceFieldAddress());
		assertEquals("collection.inner", rules.get(6).destinationFieldAddress());
		assertNull(rules.get(6).fromSourceMethod());
		assertEquals(String.class, rules.get(6).destinationField().getType());
		assertEquals(Inner.class, rules.get(6).destinationClass());
		
		assertEquals("parent", rules.get(7).sourceFieldAddress());
		assertEquals("parent", rules.get(7).destinationFieldAddress());
		assertNull(rules.get(7).fromSourceMethod());
		assertEquals(String.class, rules.get(7).destinationField().getType());
		assertEquals(Parent.class, rules.get(7).destinationClass());
		
	}
	
	@Test
	public void testMappingRuleOnObject() throws GGAPIMappingRuleException {
		List<GGAPIMappingRule> rules = GGAPIMappingRules.parse(Destination2.class);
		
		assertEquals(2, rules.size());
	}
	
}
