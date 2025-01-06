package com.garganttua.api.daos.spring.mongodb;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.geojson.GeoJsonObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Shape;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.sort.IGGAPISort;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.objects.mapper.rules.GGMappingRule;
import com.garganttua.objects.mapper.rules.GGMappingRules;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@GGBean(name = "SpringMongoDao", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPIMongoRepository implements IGGAPIDao<Object> {

	@Inject
	protected MongoTemplate mongo;
	
	private IGGAPIDomain domain;

	@Setter
	protected IGGAPIEngine engine;
	
	@Setter
	private Class<Object> dtoClass;

	@Override
	public Object save(Object object) {
		return this.mongo.save(object);
	}
	
	@Override
	public List<Object> find(IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort) {
		
		List<Object> results = new ArrayList<>();

		Query query = new Query();
	
		if (filter != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromFilter(filter);
			if( criteria != null ) {
				query.addCriteria(criteria);	
			}
			
			String textCriteriaString = GGAPIMongoRepository.getTextCriteriaFromFilter(filter);

			if( textCriteriaString != null ) {
				TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingPhrase(textCriteriaString);
				query.addCriteria(textCriteria);
			}
			
			Criteria geolocCriteria = GGAPIMongoRepository.getGeolocCriteriaFromFilter(filter);
			if( geolocCriteria != null)
				query.addCriteria(geolocCriteria);
		}
		
		
		Pageable page = null; 
		
		if( pageable != null && pageable.getPageIndex() > 0 && pageable.getPageSize() > 0 ) {
			page = PageRequest.of(pageable.getPageIndex(), pageable.getPageSize());
		} 

		if (page != null) {
			query.with(page);
		}
		
		if( sort != null ) {
			Direction direction = null;
			switch(sort.getDirection()) {
			case asc -> {direction = Sort.Direction.ASC;}
			case desc -> {direction = Sort.Direction.DESC;}
			}
			query.with(Sort.by(direction, sort.getFieldName()));
		}
		if( filter != null ) {
			log.debug("		[domain ["+this.domain.getDomain()+"]] Finding objects using "+query.toString());
		}
		results = this.mongo.find(query, this.dtoClass);

		return results;
	}

	private static String getTextCriteriaFromFilter(IGGAPIFilter filter) {
		if( filter.getName().equals(GGAPILiteral.OPERATOR_TEXT) ) {
			return (String) filter.getValue();
		} else if( !GGAPILiteral.isFinal((GGAPILiteral) filter) ){
			for(IGGAPIFilter sub: filter.getLiterals() ) {
				String text = getTextCriteriaFromFilter(sub);
				if( text != null && !text.isEmpty() ) {
					return text;
				}
			}
		}
		return null;
	}
	
	private static Criteria getGeolocCriteriaFromFilter(IGGAPIFilter filter) {
		String filterName = filter.getName();
		String fieldName = (String) filter.getValue();
		if( filterName.equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String subFilterName = filter.getLiterals().get(0).getName();
			if( subFilterName.equals(GGAPILiteral.OPERATOR_GEOLOC_SPHERE) || subFilterName.equals(GGAPILiteral.OPERATOR_GEOLOC) ) {

				try {
					GeoJsonObject geoloc = new ObjectMapper().readValue(((String) filter.getLiterals().get(0).getValue()).getBytes(), GeoJsonObject.class);
					return getCriteriaFromGeolocFilter(fieldName, geoloc, subFilterName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else if( !GGAPILiteral.isFinal((GGAPILiteral) filter) ){
			for(IGGAPIFilter sub: filter.getLiterals() ) {
				return getGeolocCriteriaFromFilter(sub);
			}
		}
		return null;
	}

	private static Criteria getCriteriaFromGeolocFilter(String geolocField, GeoJsonObject geoloc, String subFilterName) {
		Criteria criteria = null;
		
		if( geolocField != null ) {
			Shape shape = geoloc.accept(new GGAPIGeoJsonToSpringShapeBinder());
			
			criteria = Criteria.where(geolocField);
			switch(subFilterName) {
			default:
				break;
			case GGAPILiteral.OPERATOR_GEOLOC:
				criteria.within(shape);
				break;
			case GGAPILiteral.OPERATOR_GEOLOC_SPHERE:
				criteria.withinSphere((Circle) shape);
				break;
			}
		}
		
		return criteria;
	}

	/*
	 * Not very elegant, must be refactored
	 */
	private static Criteria getCriteriaFromFilter(IGGAPIFilter literal) {

		Criteria criteria = null;
		
		List<Criteria> criterias = new ArrayList<Criteria>();
		List<Object> values = new ArrayList<Object>();
		
		switch (literal.getName()) {
		case GGAPILiteral.OPERATOR_OR:
			for( IGGAPIFilter subliteral: literal.getLiterals() ) {
				Criteria crit = getCriteriaFromFilter(subliteral);
				if( crit != null )
					criterias.add(crit);
			}
			if( criterias.size() > 0 )
				criteria = new Criteria().orOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_AND:
			for( IGGAPIFilter subliteral: literal.getLiterals() ) {
				Criteria crit = getCriteriaFromFilter(subliteral);
				if( crit != null )
					criterias.add(crit);
			}
			if( criterias.size() > 0 )
				criteria = new Criteria().andOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_NOR:
			for( IGGAPIFilter subliteral: literal.getLiterals() ) {
				Criteria crit = getCriteriaFromFilter(subliteral);
				if( crit != null )
					criterias.add(crit);
			}
			if( criterias.size() > 0 )
				criteria = new Criteria().norOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_FIELD:
			
			IGGAPIFilter subLiteral = literal.getLiterals().get(0);
			
			switch(subLiteral.getName()) {
			case GGAPILiteral.OPERATOR_EQUAL:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).is(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_NOT_EQUAL:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).ne(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_GREATER_THAN:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).gt(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_GREATER_THAN_EXCLUSIVE:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).gte(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_LOWER_THAN:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).lt(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_LOWER_THAN_EXCLUSIVE:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).lte(subLiteral.getValue());
				break;
			case GGAPILiteral.OPERATOR_REGEX:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).regex(subLiteral.getValue().toString());
				break;
			case GGAPILiteral.OPERATOR_IN:
				for( IGGAPIFilter subliteral: subLiteral.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).in(values);
				break;
			case GGAPILiteral.OPERATOR_NOT_IN:
				for( IGGAPIFilter subliteral: literal.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).nin(values);
				break;
			case GGAPILiteral.OPERATOR_EMPTY:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).isNull();
				break;
			}
			break;
		}

		return criteria;
	}

	@Override
	public void delete(Object object) {
		this.mongo.remove(object);
	}

	@Override
	public long count(IGGAPIFilter filter) {
		
		Query query = new Query();

		if (filter != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromFilter(filter);
			if( criteria != null ) {
				query.addCriteria(criteria);	
			}
			
			String textCriteriaString = GGAPIMongoRepository.getTextCriteriaFromFilter(filter);

			if( textCriteriaString != null ) {
				TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingPhrase(textCriteriaString);
				query.addCriteria(textCriteria);
			}
			Criteria geolocCriteria = GGAPIMongoRepository.getGeolocCriteriaFromFilter(filter);
			if( geolocCriteria != null)
				query.addCriteria(geolocCriteria);
		}
		
		
		return this.mongo.count(query, this.dtoClass);
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
		this.domain = domain;
		if( domain.isGeolocalizedEntity() ) {
			GGObjectAddress geolocField = domain.getLocationFieldAddress();
			if( geolocField != null ) {
				
				try {
					List<GGMappingRule> rules = GGMappingRules.parse(this.dtoClass);
					
					List<GGMappingRule> templist = rules.stream().filter(rule -> {
						return rule.sourceFieldAddress().equals(geolocField);
					}).collect(Collectors.toList());
					
					Field field = GGObjectReflectionHelper.getField(this.dtoClass, templist.get(0).destinationFieldAddress().getElement(templist.get(0).destinationFieldAddress().length()-1));
					
					if( templist.size()>0 ) {
						this.mongo.indexOps(this.dtoClass).ensureIndex(new GeospatialIndex(field.getName()).typed(GeoSpatialIndexType.GEO_2DSPHERE) );
					}
					
				} catch (GGMapperException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
}
