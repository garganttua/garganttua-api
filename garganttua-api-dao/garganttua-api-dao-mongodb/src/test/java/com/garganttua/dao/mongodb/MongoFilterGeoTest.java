package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.filter.IFilter;
import com.mongodb.MongoClientSettings;

/**
 * Geospatial filter translation: a framework {@code $geoWithin} / {@code $geoWithinSphere} filter
 * carrying an {@code org.geojson} geometry becomes a MongoDB
 * {@code {<field>: {$geoWithin: {$geometry: {type,coordinates}}}}} predicate. Both operators map to
 * the same GeoJSON {@code $geometry} query (a 2dsphere index already evaluates it on the sphere).
 */
@DisplayName("MongoFilterConverter — $geoWithin / $geoWithinSphere → GeoJSON $geometry")
class MongoFilterGeoTest {

	/** Mirrors {@code Filter.operator(op, field, value)}: a $field node with one comparison child. */
	private static IFilter geoFilter(String operator, String field, Object geometry) {
		IFilter comparison = mock(IFilter.class);
		when(comparison.getName()).thenReturn(operator);
		when(comparison.getValue()).thenReturn(geometry);

		IFilter fieldFilter = mock(IFilter.class);
		when(fieldFilter.getName()).thenReturn("$field");
		when(fieldFilter.getValue()).thenReturn(field);
		when(fieldFilter.getFilters()).thenReturn(List.of(comparison));
		return fieldFilter;
	}

	private static BsonDocument render(Bson bson) {
		return bson.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
	}

	/** Navigates to {field}.$geoWithin.$geometry and returns it. */
	private static BsonDocument geometryOf(BsonDocument predicate, String field) {
		assertTrue(predicate.containsKey(field), "the predicate must key on the geolocalized field");
		BsonDocument geoWithin = predicate.getDocument(field).getDocument("$geoWithin");
		assertTrue(geoWithin.containsKey("$geometry"),
				"a GeoJSON $geoWithin must wrap the geometry in $geometry, not pass it raw");
		return geoWithin.getDocument("$geometry");
	}

	@Test
	@DisplayName("$geoWithin with a Polygon produces $geoWithin.$geometry with the exact ring coordinates")
	void polygonWithin() throws ApiException {
		Polygon square = new Polygon(
				new LngLatAlt(0, 0),
				new LngLatAlt(0, 1),
				new LngLatAlt(1, 1),
				new LngLatAlt(1, 0),
				new LngLatAlt(0, 0));

		BsonDocument predicate = render(MongoFilterConverter.convert(geoFilter("$geoWithin", "location", square)));
		BsonDocument geometry = geometryOf(predicate, "location");

		assertEquals("Polygon", geometry.getString("type").getValue());
		BsonArray rings = geometry.getArray("coordinates");
		assertEquals(1, rings.size(), "a simple polygon has exactly one (exterior) ring");
		BsonArray ring = rings.get(0).asArray();
		assertEquals(5, ring.size(), "the closed square ring has 5 positions (first == last)");
		assertEquals(0.0, ring.get(0).asArray().get(0).asDouble().getValue(), "first position lng");
		assertEquals(0.0, ring.get(0).asArray().get(1).asDouble().getValue(), "first position lat");
		assertEquals(1.0, ring.get(2).asArray().get(0).asDouble().getValue(), "third position lng");
		assertEquals(1.0, ring.get(2).asArray().get(1).asDouble().getValue(), "third position lat");
		assertEquals(ring.get(0), ring.get(4), "the ring must be closed");
	}

	@Test
	@DisplayName("$geoWithinSphere is an alias — a Point maps to the same $geometry shape")
	void sphereAliasWithPoint() throws ApiException {
		Point paris = new Point(2.3522, 48.8566);

		BsonDocument predicate = render(MongoFilterConverter.convert(geoFilter("$geoWithinSphere", "location", paris)));
		BsonDocument geometry = geometryOf(predicate, "location");

		assertEquals("Point", geometry.getString("type").getValue());
		BsonArray coords = geometry.getArray("coordinates");
		assertEquals(2.3522, coords.get(0).asDouble().getValue(), "longitude");
		assertEquals(48.8566, coords.get(1).asDouble().getValue(), "latitude");
	}

	@Test
	@DisplayName("a geo filter with no geometry value is rejected with a parlant error")
	void nullGeometryRejected() {
		ApiException ex = assertThrows(ApiException.class,
				() -> MongoFilterConverter.convert(geoFilter("$geoWithin", "location", null)));
		assertTrue(ex.getMessage().contains("location"), "the error must name the offending field");
		assertTrue(ex.getMessage().contains("GeoJSON"), "the error must mention the expected geometry");
	}
}
