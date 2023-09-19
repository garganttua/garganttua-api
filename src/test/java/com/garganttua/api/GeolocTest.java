package com.garganttua.api;

import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeolocTest {
	
	@Test
	public void test() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		
		
		Point point = new Point(47.310342, 5.062565);
		
		System.out.println(mapper.writeValueAsString(point));
		
		Circle circle = new Circle(point, new Distance(100, Metrics.KILOMETERS));
		
		System.out.println(mapper.writeValueAsString(circle));
		
		String pointString = "{ \"type\":\"Point\",\"coordinates\":[-74.008317,40.72251]}";
		
		org.geojson.Point point2 = mapper.readValue(pointString, org.geojson.Point.class);
		
		System.out.println(mapper.writeValueAsString(point2));
		
		
		
		
	}

}
