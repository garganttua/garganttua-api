package com.garganttua.api.spec.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.geojson.GeoJsonObject;
import org.geojson.GeoJsonObjectVisitor;
import org.geojson.Point;
import org.junit.jupiter.api.Test;


public class GGAPILiteralTest {
	
	@Test
	public void testEquals() {
		GGAPILiteral lit = GGAPILiteral.eq("type", "toto");
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$eq', value=toto, literals=null}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.eq("type", true);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$eq', value=true, literals=null}]}", lit2.toString());
		
		GGAPILiteral lit3 = GGAPILiteral.eq("type", 12);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$eq', value=12, literals=null}]}", lit3.toString());
	}
	
	@Test
	public void testNotEquals() {
		GGAPILiteral lit = GGAPILiteral.ne("type", "toto");
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$ne', value=toto, literals=null}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.ne("type", true);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$ne', value=true, literals=null}]}", lit2.toString());
	}
	
	@Test
	public void testSuperior() {
		GGAPILiteral lit = GGAPILiteral.gt("type", 12);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$gt', value=12, literals=null}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.gte("type", 12);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$gte', value=12, literals=null}]}", lit2.toString());
	}
	
	@Test
	public void testInferior() {
		GGAPILiteral lit = GGAPILiteral.lt("type", 12);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$lt', value=12, literals=null}]}", lit.toString());
		
		GGAPILiteral lit2 = GGAPILiteral.lte("type", 12);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$lte', value=12, literals=null}]}", lit2.toString());
	}
	
	@Test
	public void testEmpty() {
		GGAPILiteral lit = GGAPILiteral.empty("type");
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$empty', value=null, literals=null}]}", lit.toString());
		
	}
	
	@Test
	public void testRegex() {
		String regex = "\\w+\\.\\w+";
		GGAPILiteral lit = GGAPILiteral.regex("type", regex);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$regex', value=\\w+\\.\\w+, literals=null}]}", lit.toString());
		
	}
	
	@Test
	public void testIn() throws GGAPILiteralException {
		GGAPILiteral lit = GGAPILiteral.in("type", 12, 13, 15);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$in', value=null, literals=[GGAPILiteral{name='null', value=12, literals=null}, GGAPILiteral{name='null', value=13, literals=null}, GGAPILiteral{name='null', value=15, literals=null}]}]}", lit.toString());
		
		GGAPILiteral.validate(lit);
	}
	
	@Test
	public void testNotIn() throws GGAPILiteralException {
		GGAPILiteral lit = GGAPILiteral.nin("type", 12, 13, 15);
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$nin', value=null, literals=[GGAPILiteral{name='null', value=12, literals=null}, GGAPILiteral{name='null', value=13, literals=null}, GGAPILiteral{name='null', value=15, literals=null}]}]}", lit.toString());
		
		GGAPILiteral.validate(lit);
	}
	
	@Test
	public void testText() throws GGAPILiteralException {
		GGAPILiteral lit = GGAPILiteral.text("type", "the text");
		
		assertEquals("GGAPILiteral{name='$field', value=type, literals=[GGAPILiteral{name='$text', value=the text, literals=null}]}", lit.toString());
	}
	
	@Test
	public void testAnd() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral and = GGAPILiteral.and(lit, lit2);
		
		assertEquals("GGAPILiteral{name='$and', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", and.toString());
	}
	
	@Test
	public void testOr() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral or = GGAPILiteral.or(lit, lit2);
		
		assertEquals("GGAPILiteral{name='$or', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", or.toString());
	}
	
	@Test
	public void testNor() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral nor = GGAPILiteral.nor(lit, lit2);
		
		assertEquals("GGAPILiteral{name='$nor', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", nor.toString());
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
		
		assertEquals("GGAPILiteral{name='$and', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", andLit.toString());
	
		GGAPILiteral andLit2 = andLit.andOperator(lit3);
		
		assertEquals("GGAPILiteral{name='$and', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", andLit2.toString());
	
		GGAPILiteral and = GGAPILiteral.and();
		GGAPILiteral and2 = and.andOperator();
		
		assertEquals("GGAPILiteral{name='$and', value=null, literals=[]}", and2.toString());
	
	}
	
	@Test
	public void testOrOperator() {

		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral orLit = lit.orOperator(lit2);
		
		assertEquals("GGAPILiteral{name='$or', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", orLit.toString());
	
		GGAPILiteral orLit2 = orLit.orOperator(lit3);
		
		assertEquals("GGAPILiteral{name='$or', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", orLit2.toString());
	
		GGAPILiteral or = GGAPILiteral.or();
		GGAPILiteral or2 = or.orOperator();
		
		assertEquals("GGAPILiteral{name='$or', value=null, literals=[]}", or2.toString());
	
	}
	
	@Test
	public void testNorOperator() {

		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral norLit = lit.norOperator(lit2);
		
		assertEquals("GGAPILiteral{name='$nor', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", norLit.toString());
	
		GGAPILiteral norLit2 = norLit.norOperator(lit3);
		
		assertEquals("GGAPILiteral{name='$nor', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}", norLit2.toString());
	
		GGAPILiteral nor = GGAPILiteral.nor();
		GGAPILiteral nor2 = nor.norOperator();
		
		assertEquals("GGAPILiteral{name='$nor', value=null, literals=[]}", nor2.toString());
	
	}
	
	@Test
	public void testMixOperator() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit4 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral filter = lit.andOperator(lit2).orOperator(lit3, lit4);
		
		assertEquals("GGAPILiteral{name='$or', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$and', value=null, literals=[GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}, GGAPILiteral{name='$field', value=fieldName, literals=[GGAPILiteral{name='$eq', value=fieldValue, literals=null}]}]}]}", filter.toString());
		
	}
	
	@Test
	public void testClone() {
		GGAPILiteral lit = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit2 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit3 = GGAPILiteral.eq("fieldName", "fieldValue");
		GGAPILiteral lit4 = GGAPILiteral.eq("fieldName", "fieldValue");
		
		GGAPILiteral filter = lit.andOperator(lit2).orOperator(lit3, lit4);
		
		GGAPILiteral filterCloned = (GGAPILiteral) filter.clone();
		
		assertEquals(filter, filterCloned);
	}
	
	@Test
	public void testGeoloc() throws GGAPILiteralException {
		String geoString = "{"
				+ "  \"type\": \"Feature\","
				+ "  \"properties\": {"
				+ "    \"radius\": 443.0003055263856"
				+ "  },"
				+ "  \"geometry\": { \"type\": \"Point\", \"coordinates\": [-74.008317, 40.72251] }"
				+ "}";
		GeoJsonObject test = new Point(0,0);
				
//		GeoJsonObject object = new ObjectMapper().readValue(geoString , GeoJsonObject.class);
		
		GGAPILiteral lit = GGAPILiteral.geolocWithin("fieldName", test);
		
		GGAPILiteral.validate(lit);
		
		String geoString2 = lit.toString();
		System.out.println(geoString2);
		
//		GGAPILiteral lit2 = new ObjectMapper().readValue(geoString2 , GGAPILiteral.class);
		
	}
	
}
