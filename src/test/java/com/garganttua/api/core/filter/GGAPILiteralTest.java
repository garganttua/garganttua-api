package com.garganttua.api.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.dao.GGAPILiteralException;


public class GGAPILiteralTest {
	
	@Test
	public void testEquals() {
		GGAPILiteral lit = GGAPILiteral.eq("type", "toto");
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$eq\",\"value\":\"toto\"}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.eq("type", true);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$eq\",\"value\":true}]}", lit2.toString());
		
		GGAPILiteral lit3 = GGAPILiteral.eq("type", 12);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$eq\",\"value\":12}]}", lit3.toString());
	}
	
	@Test
	public void testNotEquals() {
		GGAPILiteral lit = GGAPILiteral.ne("type", "toto");
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$ne\",\"value\":\"toto\"}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.ne("type", true);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$ne\",\"value\":true}]}", lit2.toString());
	}
	
	@Test
	public void testSuperior() {
		GGAPILiteral lit = GGAPILiteral.gt("type", 12);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$gt\",\"value\":12}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.gte("type", 12);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$gte\",\"value\":12}]}", lit2.toString());
	}
	
	@Test
	public void testInferior() {
		GGAPILiteral lit = GGAPILiteral.lt("type", 12);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$lt\",\"value\":12}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.lte("type", 12);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$lte\",\"value\":12}]}", lit2.toString());
	}
	
	@Test
	public void testEmpty() {
		GGAPILiteral lit = GGAPILiteral.empty("type");
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$empty\"}]}", lit.toString());
		
	}
	
	@Test
	public void testRegex() {
		String regex = "\\w+\\.\\w+";
		GGAPILiteral lit = GGAPILiteral.regex("type", regex);
		
//		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$regex\",\"value\":\""+regex+"\"}]}", lit.toString());
		
	}
	
	@Test
	public void testIn() throws GGAPILiteralException {
		GGAPILiteral lit = GGAPILiteral.in("type", 12, 13, 15);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$in\",\"literals\":[{\"value\":12},{\"value\":13},{\"value\":15}]}]}", lit.toString());
		
		GGAPILiteral.validate(lit);
	}
	
	@Test
	public void testNotIn() throws GGAPILiteralException {
		GGAPILiteral lit = GGAPILiteral.nin("type", 12, 13, 15);
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$nin\",\"literals\":[{\"value\":12},{\"value\":13},{\"value\":15}]}]}", lit.toString());
		
		GGAPILiteral.validate(lit);
	}
	
	@Test
	public void testText() throws GGAPILiteralException {
		GGAPILiteral lit = GGAPILiteral.text("type", "the text");
		
		assertEquals("{\"name\":\"$field\",\"value\":\"type\",\"literals\":[{\"name\":\"$text\",\"value\":\"the text\"}]}", lit.toString());
	}
	
	@Test
	public void testAnd() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral and = GGAPILiteral.and(lit, lit2);
		
		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", and.toString());
	}
	
	@Test
	public void testOr() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral or = GGAPILiteral.or(lit, lit2);
		
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", or.toString());
	}
	
	@Test
	public void testNor() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral nor = GGAPILiteral.nor(lit, lit2);
		
		assertEquals("{\"name\":\"$nor\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", nor.toString());
	}
	
	@Test
	public void testRegex__() {
		String regex = "toto";
		Pattern pattern = Pattern.compile(regex);
	}
	
	@Test
	public void testAndOperator() {

		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral andLit = lit.andOperator(lit2);
		
		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", andLit.toString());
	
		GGAPILiteral andLit2 = andLit.andOperator(lit3);
		
		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", andLit2.toString());
	
		GGAPILiteral and = GGAPILiteral.and();
		GGAPILiteral and2 = and.andOperator();
		
		assertEquals("{\"name\":\"$and\",\"literals\":[]}", and2.toString());
	
	}
	
	@Test
	public void testOrOperator() {

		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral orLit = lit.orOperator(lit2);
		
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", orLit.toString());
	
		GGAPILiteral orLit2 = orLit.orOperator(lit3);
		
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", orLit2.toString());
	
		GGAPILiteral or = GGAPILiteral.or();
		GGAPILiteral or2 = or.orOperator();
		
		assertEquals("{\"name\":\"$or\",\"literals\":[]}", or2.toString());
	
	}
	
	@Test
	public void testNorOperator() {

		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral norLit = lit.norOperator(lit2);
		
		assertEquals("{\"name\":\"$nor\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", norLit.toString());
	
		GGAPILiteral norLit2 = norLit.norOperator(lit3);
		
		assertEquals("{\"name\":\"$nor\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}", norLit2.toString());
	
		GGAPILiteral nor = GGAPILiteral.nor();
		GGAPILiteral nor2 = nor.norOperator();
		
		assertEquals("{\"name\":\"$nor\",\"literals\":[]}", nor2.toString());
	
	}
	
	@Test
	public void testMixOperator() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit4 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral filter = lit.andOperator(lit2).orOperator(lit3, lit4);
		
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]},{\"name\":\"$field\",\"value\":\"fieldName\",\"literals\":[{\"name\":\"$eq\",\"value\":\"fieldValue\"}]}]}]}", filter.toString());
		
	}
	
	@Test
	public void testGeoloc() throws JsonMappingException, JsonProcessingException, GGAPILiteralException {
		String geoString = "{"
				+ "  \"type\": \"Feature\","
				+ "  \"properties\": {"
				+ "    \"radius\": 443.0003055263856"
				+ "  },"
				+ "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [-74.008317, 40.72251] }"
				+ "}";
		GeoJsonObject object = new ObjectMapper().readValue(geoString , GeoJsonObject.class);
		
		GGAPILiteral lit = GGAPILiteral.geolocWithin("fieldName", object);
		
		GGAPILiteral.validate(lit);
		
		String geoString2 = lit.toString();
		
		GGAPILiteral lit2 = new ObjectMapper().readValue(geoString2 , GGAPILiteral.class);
		
	}
	
}
