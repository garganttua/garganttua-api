package com.garganttua.api.spec.filter;

import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GGAPIGeolocFilter {
	
	public static final String CIRCLE_RADIUS = "radius";
	
	@JsonProperty
	private GGAPIGeolocFilterType type;
	
	@JsonProperty
	private GeoJsonObject shape;

	public static void validate(GGAPIGeolocFilter geoloc) throws GGAPIGeolocFilterException {
		// TODO Auto-generated method stub
		
	}
	
}
