package com.garganttua.api.daos.mongodb;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObjectVisitor;
import org.geojson.GeometryCollection;
import org.geojson.LineString;
import org.geojson.MultiLineString;
import org.geojson.MultiPoint;
import org.geojson.MultiPolygon;
import org.geojson.Point;
import org.geojson.Polygon;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class GGAPIGeoJsonToMongoGeoJsonBinder implements GeoJsonObjectVisitor<GeoJson<?>>{

	@Override
	public GeoJson<?> visit(GeometryCollection geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(FeatureCollection geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(Point geoJsonObject) {	
		org.springframework.data.geo.Point point = (org.springframework.data.geo.Point) geoJsonObject.accept(new GGAPIGeoJsonPointToSpringPointBinder());
		GeoJsonPoint p = new GeoJsonPoint(point);
		return p;
	}

	@Override
	public GeoJson<?> visit(Feature geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(MultiLineString geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(Polygon geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(MultiPolygon geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(MultiPoint geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoJson<?> visit(LineString geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

}
