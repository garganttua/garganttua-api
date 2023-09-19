package com.garganttua.api.repository.dao.mongodb;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObjectVisitor;
import org.geojson.GeometryCollection;
import org.geojson.LineString;
import org.geojson.MultiLineString;
import org.geojson.MultiPoint;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Shape;

public class GGAPIGeoJsonPointToSpringPointBinder implements GeoJsonObjectVisitor<Point>{

	@Override
	public Point visit(GeometryCollection geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(FeatureCollection geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(org.geojson.Point geoJsonObject) {
		Point p = new Point(geoJsonObject.getCoordinates().getLongitude(), geoJsonObject.getCoordinates().getLatitude());
		return p;
	}

	@Override
	public Point visit(Feature geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(MultiLineString geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(Polygon geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(MultiPolygon geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(MultiPoint geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point visit(LineString geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

}
