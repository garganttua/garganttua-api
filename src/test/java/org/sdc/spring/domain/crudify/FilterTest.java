package org.sdc.spring.domain.crudify;

import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.spec.filter.SpringCrudifyLiteral;
import com.garganttua.api.spec.filter.SpringCrudifyLiteralException;
import com.mongodb.client.model.Filters;

public class FilterTest {

	@Test
	public void testFilter() {
		
		Bson filter = Filters.eq("field1", "value1");
		Bson filter2 = Filters.gt("field2", "value2");
		Bson filter3 = Filters.gte("field3", "value3");
		Bson filter4 = Filters.lt("field4", "value4");
		Bson filter5 = Filters.lte("field5", "value5");
		Bson filter6 = Filters.ne("field6", "value6");
		Bson filter7 = Filters.in("field7", "value7", "value8");
		Bson filter8 = Filters.nin("field8", "value9", "value10");
		Bson filter9 = Filters.regex("field9", "regex");
		Bson filter10 = Filters.empty();
		
		Bson filter11  = Filters.and(filter, filter2, filter3, filter4, filter5);
		Bson filter12  = Filters.and(filter6, filter7, filter8, filter9, filter10);
		
		Bson filter13  = Filters.or(filter11, filter12);
		Bson filter14  = Filters.nor(filter11, filter12);
		
		Bson filter15  = Filters.and(filter13, filter14);
		
		System.out.println(filter15.toBsonDocument().toString());
	
	}
	
	
	@Test
	public void testCReateFilterFromString() throws JsonMappingException, JsonProcessingException, SpringCrudifyLiteralException {
		
		String t = "{\"name\":\"$and\","
				+ "\"literals\":["
				+ "{\"name\":\"$field\", \"value\":\"techName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"chlapeune\"}]},"
				+ "{\"name\":\"$field\", \"value\":\"techName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"chlapeune\"}]}"
				+ "]"
				+ "}";
		ObjectMapper mapper = new ObjectMapper();

		SpringCrudifyLiteral filter = mapper.readValue(t, SpringCrudifyLiteral.class);
		
		System.out.println(t);
		SpringCrudifyLiteral.validate(filter);
		
	}
	
	
	
}
