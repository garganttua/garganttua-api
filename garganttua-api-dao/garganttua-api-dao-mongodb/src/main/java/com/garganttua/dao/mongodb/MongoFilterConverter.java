package com.garganttua.dao.mongodb;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.filter.IFilter;
import com.mongodb.client.model.Filters;

public class MongoFilterConverter {

	public static Bson convert(IFilter filter) throws ApiException {
		if (filter == null) {
			return new Document();
		}

		String name = filter.getName();
		if (name == null) {
			return new Document();
		}

		return switch (name) {
			case "$and" -> convertLogical(filter, "$and");
			case "$or" -> convertLogical(filter, "$or");
			case "$nor" -> convertLogical(filter, "$nor");
			case "$field" -> convertField(filter);
			default -> throw new ApiException("Unsupported filter operator: " + name);
		};
	}

	private static Bson convertLogical(IFilter filter, String operator) throws ApiException {
		List<IFilter> subs = filter.getFilters();
		if (subs == null || subs.size() < 2) {
			throw new ApiException("Logical operator " + operator + " requires at least 2 sub-filters");
		}

		Bson[] converted = new Bson[subs.size()];
		for (int i = 0; i < subs.size(); i++) {
			converted[i] = convert(subs.get(i));
		}

		return switch (operator) {
			case "$and" -> Filters.and(converted);
			case "$or" -> Filters.or(converted);
			case "$nor" -> Filters.nor(converted);
			default -> throw new ApiException("Unknown logical operator: " + operator);
		};
	}

	private static Bson convertField(IFilter filter) throws ApiException {
		String fieldName = (String) filter.getValue();
		if (fieldName == null) {
			throw new ApiException("$field filter requires a field name as value");
		}

		List<IFilter> subs = filter.getFilters();
		if (subs == null || subs.size() != 1) {
			throw new ApiException("$field filter requires exactly 1 comparison sub-filter");
		}

		IFilter comparison = subs.get(0);
		String op = comparison.getName();
		Object value = comparison.getValue();

		return switch (op) {
			case "$eq" -> Filters.eq(fieldName, value);
			case "$ne" -> Filters.ne(fieldName, value);
			case "$gt" -> Filters.gt(fieldName, value);
			case "$gte" -> Filters.gte(fieldName, value);
			case "$lt" -> Filters.lt(fieldName, value);
			case "$lte" -> Filters.lte(fieldName, value);
			case "$regex" -> Filters.regex(fieldName, value.toString());
			case "$empty" -> Filters.exists(fieldName, false);
			case "$in" -> convertIn(fieldName, comparison);
			case "$nin" -> convertNin(fieldName, comparison);
			case "$text" -> Filters.text(value.toString());
			default -> throw new ApiException("Unsupported comparison operator: " + op);
		};
	}

	private static Bson convertIn(String fieldName, IFilter comparison) throws ApiException {
		List<IFilter> values = comparison.getFilters();
		if (values == null || values.isEmpty()) {
			throw new ApiException("$in operator requires at least 1 value");
		}
		List<Object> inValues = values.stream().map(IFilter::getValue).toList();
		return Filters.in(fieldName, inValues);
	}

	private static Bson convertNin(String fieldName, IFilter comparison) throws ApiException {
		List<IFilter> values = comparison.getFilters();
		if (values == null || values.isEmpty()) {
			throw new ApiException("$nin operator requires at least 1 value");
		}
		List<Object> ninValues = values.stream().map(IFilter::getValue).toList();
		return Filters.nin(fieldName, ninValues);
	}
}
