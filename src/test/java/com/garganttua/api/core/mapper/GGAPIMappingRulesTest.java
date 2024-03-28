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
		
		assertEquals(7, rules.size());
		
		assertEquals("field", rules.get(0).sourceFieldAddress().toString());
		assertEquals("field", rules.get(0).destinationFieldAddress().toString());
		assertNull(rules.get(0).fromSourceMethodAddress());
		assertEquals(Destination.class, rules.get(0).destinationClass());
		
		assertEquals("inner", rules.get(1).sourceFieldAddress().toString());
		assertEquals("list.inner", rules.get(1).destinationFieldAddress().toString());
		assertNull(rules.get(1).fromSourceMethodAddress());
		assertEquals(Inner.class, rules.get(1).destinationClass());
		
		assertEquals("inner", rules.get(2).sourceFieldAddress().toString());
		assertEquals("map1.#value.inner", rules.get(2).destinationFieldAddress().toString());
		assertNull(rules.get(2).fromSourceMethodAddress());
		assertEquals(Inner.class, rules.get(2).destinationClass());
		
		assertEquals("inner", rules.get(3).sourceFieldAddress().toString());
		assertEquals("map2.#key.inner", rules.get(3).destinationFieldAddress().toString());
		assertNull(rules.get(3).fromSourceMethodAddress());
		assertEquals(Inner.class, rules.get(3).destinationClass());
		
		assertEquals("inner", rules.get(4).sourceFieldAddress().toString());
		assertEquals("set.inner", rules.get(4).destinationFieldAddress().toString());
		assertNull(rules.get(4).fromSourceMethodAddress());
		assertEquals(Inner.class, rules.get(4).destinationClass());
		
		assertEquals("inner", rules.get(5).sourceFieldAddress().toString());
		assertEquals("collection.inner", rules.get(5).destinationFieldAddress().toString());
		assertNull(rules.get(5).fromSourceMethodAddress());
		assertEquals(Inner.class, rules.get(5).destinationClass());
		
		assertEquals("parent", rules.get(6).sourceFieldAddress().toString());
		assertEquals("parent", rules.get(6).destinationFieldAddress().toString());
		assertNull(rules.get(6).fromSourceMethodAddress());
		assertEquals(Parent.class, rules.get(6).destinationClass());
	}
	
	@Test
	public void testMappingRuleOnObject() throws GGAPIMappingRuleException {
		List<GGAPIMappingRule> rules = GGAPIMappingRules.parse(Destination2.class);
		
		assertEquals(3, rules.size());
	}
	
}
