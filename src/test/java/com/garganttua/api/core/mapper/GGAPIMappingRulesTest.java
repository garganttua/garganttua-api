package com.garganttua.api.core.mapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

class DestinationWithMappingfromFieldThatDoesntExist{
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "notExists")
	private String field;

}

class Source {
	private int field;
}

class DestinationWithIncorrectFromMethod{
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "field", fromSourceMethod = "from")
	private String field;
	
	public String from(String field) {
		return "";
	}
}

class DestinationWithIncorrectToMethod{
	@GGAPIFieldMappingRule(sourceFieldAddress = "field", toSourceMethod = "to")
	private String field;
	
	public String to(String field) {
		return "";
	}
}

class DestinationWithNoToMethod{
	@GGAPIFieldMappingRule(sourceFieldAddress = "field", toSourceMethod = "to")
	private String field;

}

class CorrectDestination {
	
	@GGAPIFieldMappingRule(sourceFieldAddress = "field", fromSourceMethod = "from", toSourceMethod = "to")
	private String field;
	
	public String from(int field) {
		return "";
		
	}
	public int to(String field) {
		return 1;
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
	
	@Test
	public void testValidate() throws GGAPIMappingRuleException {
		List<GGAPIMappingRule> rules = GGAPIMappingRules.parse(CorrectDestination.class);
		assertEquals(1, rules.size());
		
		assertDoesNotThrow(() -> {
			GGAPIMappingRules.validate(Source.class, rules);
		});
		
		List<GGAPIMappingRule> rules2 = GGAPIMappingRules.parse(DestinationWithMappingfromFieldThatDoesntExist.class);
		assertEquals(1, rules2.size());

		GGAPIMappingRuleException exception = assertThrows( GGAPIMappingRuleException.class , () -> GGAPIMappingRules.validate(Destination2.class, rules2));
		
		assertNotNull(exception);
		assertEquals("com.garganttua.api.core.objects.query.GGAPIObjectQueryException: Object element notExists not found in class class java.lang.Object", exception.getMessage());
	
		List<GGAPIMappingRule> rules3 = GGAPIMappingRules.parse(DestinationWithIncorrectFromMethod.class);
		assertEquals(1, rules3.size());
	
		GGAPIMappingRuleException exception2 = assertThrows( GGAPIMappingRuleException.class , () -> GGAPIMappingRules.validate(Source.class, rules3));
		
		assertNotNull(exception2);
		assertEquals("Invalid method from of class DestinationWithIncorrectFromMethod : parameter must be of type int", exception2.getMessage());

		List<GGAPIMappingRule> rules4 = GGAPIMappingRules.parse(DestinationWithIncorrectToMethod.class);
		assertEquals(1, rules4.size());
	
		GGAPIMappingRuleException exception3 = assertThrows( GGAPIMappingRuleException.class , () -> GGAPIMappingRules.validate(Source.class, rules4));
		
		assertNotNull(exception3);
		assertEquals("Invalid method to of class DestinationWithIncorrectToMethod : return type must be int", exception3.getMessage());
		
		List<GGAPIMappingRule> rules5 = GGAPIMappingRules.parse(DestinationWithNoToMethod.class);
		assertEquals(1, rules5.size());
	
		GGAPIMappingRuleException exception4 = assertThrows( GGAPIMappingRuleException.class , () -> GGAPIMappingRules.validate(Source.class, rules5));
		
		assertNotNull(exception4);
		assertEquals("com.garganttua.api.core.objects.query.GGAPIObjectQueryException: Object element to not found in class class java.lang.Object", exception4.getMessage());
		
	
	}
	
}
