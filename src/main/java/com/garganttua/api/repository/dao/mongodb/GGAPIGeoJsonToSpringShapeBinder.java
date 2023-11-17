package com.garganttua.api.repository.dao.mongodb;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Shape;

import com.garganttua.api.spec.filter.GGAPIGeolocFilter;

public class GGAPIGeoJsonToSpringShapeBinder implements GeoJsonObjectVisitor<Shape> {

	@Override
	public Shape visit(GeometryCollection geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape visit(FeatureCollection geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape visit(Point geoJsonObject) {
		return null;
	}

	@Override
	public Shape visit(Feature geoJsonObject) {
	
		Point point = (Point) geoJsonObject.getGeometry();
		double radius = geoJsonObject.getProperty(GGAPIGeolocFilter.CIRCLE_RADIUS);
		double y = point.getCoordinates().getLatitude();
		double x = point.getCoordinates().getLongitude();
		
		return new Circle(x, y, radius);
	}

	@Override
	public Shape visit(MultiLineString geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape visit(Polygon geoJsonObject) {
		List<org.springframework.data.geo.Point> points = new ArrayList<org.springframework.data.geo.Point>();
		
		geoJsonObject.getCoordinates().forEach(coordinates -> {
			coordinates.forEach(point -> {
				points.add(new org.springframework.data.geo.Point(point.getLongitude(), point.getLatitude()));
			});
		});
		
		org.springframework.data.geo.Polygon polygon = new org.springframework.data.geo.Polygon(points);
		return polygon;
	}

	@Override
	public Shape visit(MultiPolygon geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape visit(MultiPoint geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape visit(LineString geoJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

}
