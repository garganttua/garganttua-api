package com.garganttua.api.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.regex.Pattern;

import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class LiteralTest {
	
	@Test
	public void testJsonSerialisation() throws JsonParseException, JsonMappingException, IOException  {
		String filter = "{\"name\":\"$field\", \"value\":\"type\", \"literals\":[{\"name\":\"$eq\", \"value\":\"true\"}]}";
		System.out.println(filter);
		ObjectMapper mapper = new ObjectMapper();
		Literal lit = mapper.readValue(filter.getBytes(), Literal.class);
	}
	
	@Test
	public void testEquals() {
		Literal lit = Literal.eq("type", "toto");
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$eq', value=toto, literals=null}]}", lit.toString());
		
		Literal lit2 = Literal.eq("type", true);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$eq', value=true, literals=null}]}", lit2.toString());
		
		Literal lit3 = Literal.eq("type", 12);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$eq', value=12, literals=null}]}", lit3.toString());
	}
	
	@Test
	public void testNotEquals() {
		Literal lit = Literal.ne("type", "toto");
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$ne', value=toto, literals=null}]}", lit.toString());
		
		Literal lit2 = Literal.ne("type", true);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$ne', value=true, literals=null}]}", lit2.toString());
	}
	
	@Test
	public void testSuperior() {
		Literal lit = Literal.gt("type", 12);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$gt', value=12, literals=null}]}", lit.toString());
		
		Literal lit2 = Literal.gte("type", 12);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$gte', value=12, literals=null}]}", lit2.toString());
	}
	
	@Test
	public void testInferior() {
		Literal lit = Literal.lt("type", 12);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$lt', value=12, literals=null}]}", lit.toString());
		
		Literal lit2 = Literal.lte("type", 12);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$lte', value=12, literals=null}]}", lit2.toString());
	}
	
	@Test
	public void testEmpty() {
		Literal lit = Literal.empty("type");
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$empty', value=null, literals=null}]}", lit.toString());
		
	}
	
	@Test
	public void testRegex() {
		String regex = "\\w+\\.\\w+";
		Literal lit = Literal.regex("type", regex);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$regex', value=\\w+\\.\\w+, literals=null}]}", lit.toString());
		
	}
	
	@Test
	public void testIn() throws LiteralException {
		Literal lit = Literal.in("type", 12, 13, 15);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$in', value=null, literals=[Literal{name='null', value=12, literals=null}, Literal{name='null', value=13, literals=null}, Literal{name='null', value=15, literals=null}]}]}", lit.toString());
		
		Literal.validate(lit);
	}
	
	@Test
	public void testNotIn() throws LiteralException {
		Literal lit = Literal.nin("type", 12, 13, 15);
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$nin', value=null, literals=[Literal{name='null', value=12, literals=null}, Literal{name='null', value=13, literals=null}, Literal{name='null', value=15, literals=null}]}]}", lit.toString());
		
		Literal.validate(lit);
	}
	
	@Test
	public void testText() throws LiteralException {
		Literal lit = Literal.text("type", "the text");
		
		assertEquals("Literal{name='$field', value=type, literals=[Literal{name='$text', value=the text, literals=null}]}", lit.toString());
	}
	
	@Test
	public void testAnd() {
		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal and = Literal.and(lit, lit2);
		
		assertEquals("Literal{name='$and', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", and.toString());
	}
	
	@Test
	public void testOr() {
		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal or = Literal.or(lit, lit2);
		
		assertEquals("Literal{name='$or', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", or.toString());
	}
	
	@Test
	public void testNor() {
		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal nor = Literal.nor(lit, lit2);
		
		assertEquals("Literal{name='$nor', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", nor.toString());
	}
	
	@Test
	public void testRegex__() {
		String regex = "toto";
		Pattern pattern = Pattern.compile(regex);
	}
	
	@Test
	public void testAndOperator() {

		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal lit3 = Literal.eq("fieldName", "fieldValue");
		
		Literal andLit = lit.andOperator(lit2);
		
		assertEquals("Literal{name='$and', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", andLit.toString());
	
		Literal andLit2 = andLit.andOperator(lit3);
		
		assertEquals("Literal{name='$and', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", andLit2.toString());
	
		Literal and = Literal.and();
		Literal and2 = and.andOperator();
		
		assertEquals("Literal{name='$and', value=null, literals=[]}", and2.toString());
	
	}
	
	@Test
	public void testOrOperator() {

		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal lit3 = Literal.eq("fieldName", "fieldValue");
		
		Literal orLit = lit.orOperator(lit2);
		
		assertEquals("Literal{name='$or', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", orLit.toString());
	
		Literal orLit2 = orLit.orOperator(lit3);
		
		assertEquals("Literal{name='$or', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", orLit2.toString());
	
		Literal or = Literal.or();
		Literal or2 = or.orOperator();
		
		assertEquals("Literal{name='$or', value=null, literals=[]}", or2.toString());
	
	}
	
	@Test
	public void testNorOperator() {

		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal lit3 = Literal.eq("fieldName", "fieldValue");
		
		Literal norLit = lit.norOperator(lit2);
		
		assertEquals("Literal{name='$nor', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", norLit.toString());
	
		Literal norLit2 = norLit.norOperator(lit3);
		
		assertEquals("Literal{name='$nor', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}", norLit2.toString());
	
		Literal nor = Literal.nor();
		Literal nor2 = nor.norOperator();
		
		assertEquals("Literal{name='$nor', value=null, literals=[]}", nor2.toString());
	
	}
	
	@Test
	public void testMixOperator() {
		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal lit3 = Literal.eq("fieldName", "fieldValue");
		Literal lit4 = Literal.eq("fieldName", "fieldValue");
		
		Literal filter = lit.andOperator(lit2).orOperator(lit3, lit4);
		
		assertEquals("Literal{name='$or', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$and', value=null, literals=[Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}, Literal{name='$field', value=fieldName, literals=[Literal{name='$eq', value=fieldValue, literals=null}]}]}]}", filter.toString());
		
	}
	
	@Test
	public void testClone() {
		Literal lit = Literal.eq("fieldName", "fieldValue");
		Literal lit2 = Literal.eq("fieldName", "fieldValue");
		Literal lit3 = Literal.eq("fieldName", "fieldValue");
		Literal lit4 = Literal.eq("fieldName", "fieldValue");
		
		Literal filter = lit.andOperator(lit2).orOperator(lit3, lit4);
		
		Literal filterCloned = (Literal) filter.clone();
		
		assertEquals(filter, filterCloned);
	}
	
	@Test
	public void testGeoloc() throws LiteralException {
		String geoString = "{"
				+ "  \"type\": \"Feature\","
				+ "  \"properties\": {"
				+ "    \"radius\": 443.0003055263856"
				+ "  },"
				+ "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [-74.008317, 40.72251] }"
				+ "}";
		GeoJsonObject test = new Point(0,0);
				
//		GeoJsonObject object = new ObjectMapper().readValue(geoString , GeoJsonObject.class);
		
		Literal lit = Literal.geolocWithin("fieldName", test);
		
		Literal.validate(lit);
		
		String geoString2 = lit.toString();
		System.out.println(geoString2);
		
//		Literal lit2 = new ObjectMapper().readValue(geoString2 , Literal.class);
		
	}
	
}
