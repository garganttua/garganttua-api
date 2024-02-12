package com.garganttua.api.repository.dao.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
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

import com.garganttua.api.core.GGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.mongodb.client.model.Filters;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIMongoRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIDAORepository<Entity, Dto> {

	protected MongoTemplate mongo;

	@Setter
	protected IGGAPIEngine engine;
	
	public void setMongoTemplate(MongoTemplate mongo) {
		this.mongo = mongo;
	}
	
	@Override
	public Dto save(Dto object) {
		return this.mongo.save(object);
	}
	
	@Override
	public List<Dto> find(Pageable pageable, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc) {
		
		List<Dto> results = new ArrayList<>();

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
		}
		
		if (geoloc != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromGeolocFilter(geoloc, this.dynamicDomain.geolocalized());
			if( criteria != null)
				query.addCriteria(criteria);
		}

		if (pageable != null) {
			query.with(pageable);
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
			log.debug("		[domain ["+this.dynamicDomain.domain()+"]] Finding objects using "+query.toString());
		}
		results = this.mongo.find(query, this.dtoClass);

		return results;
	}

	private static String getTextCriteriaFromFilter(GGAPILiteral filter) {
		if( filter.getName().equals(GGAPILiteral.OPERATOR_TEXT) ) {
			return (String) filter.getValue();
		} else if( !GGAPILiteral.isFinal(filter) ){
			for(GGAPILiteral sub: filter.getLiterals() ) {
				String text = getTextCriteriaFromFilter(sub);
				if( text != null && !text.isEmpty() ) {
					return text;
				}
			}
		}
		return null;
	}

	private static Criteria getCriteriaFromGeolocFilter(GGAPIGeolocFilter geoloc, String geolocField) {
		Criteria criteria = null;
		
		if( geolocField != null ) {
			Shape shape = geoloc.getShape().accept(new GGAPIGeoJsonToSpringShapeBinder());
			
			criteria = Criteria.where(geolocField);
			switch(geoloc.getType()) {
			default:
				break;
			case GEO_WITHIN:
				criteria.within(shape);
				break;
			case GEO_WITHIN_SPHERE:
				criteria.withinSphere((Circle) shape);
				break;
			}
		}
		
		return criteria;
	}

	/*
	 * Not very elegant, must be refactored
	 */
	private static Criteria getCriteriaFromFilter(GGAPILiteral literal) {

		Criteria criteria = null;
		
		List<Criteria> criterias = new ArrayList<Criteria>();
		List<Object> values = new ArrayList<Object>();
		
		switch (literal.getName()) {
		case GGAPILiteral.OPERATOR_OR:
			for( GGAPILiteral subliteral: literal.getLiterals() ) {
				Criteria crit = getCriteriaFromFilter(subliteral);
				if( crit != null )
					criterias.add(crit);
			}
			if( criterias.size() > 0 )
				criteria = new Criteria().orOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_AND:
			for( GGAPILiteral subliteral: literal.getLiterals() ) {
				Criteria crit = getCriteriaFromFilter(subliteral);
				if( crit != null )
					criterias.add(crit);
			}
			if( criterias.size() > 0 )
				criteria = new Criteria().andOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_NOR:
			for( GGAPILiteral subliteral: literal.getLiterals() ) {
				Criteria crit = getCriteriaFromFilter(subliteral);
				if( crit != null )
					criterias.add(crit);
			}
			if( criterias.size() > 0 )
				criteria = new Criteria().norOperator(criterias);
			break;
		case GGAPILiteral.OPERATOR_FIELD:
			
			GGAPILiteral subLiteral = literal.getLiterals().get(0);
			
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
				for( GGAPILiteral subliteral: subLiteral.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).in(values);
				break;
			case GGAPILiteral.OPERATOR_NOT_IN:
				for( GGAPILiteral subliteral: literal.getLiterals() ) {
					values.add(subliteral.getValue());
				}
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).nin(values);
				break;
			case GGAPILiteral.OPERATOR_EMPTY:
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).isNullValue();
				break;
			}
			break;
		}

		return criteria;
	}

	@Override
	public void delete(Dto object) {
		this.mongo.remove(object);
	}

	@Override
	public long count(GGAPILiteral filter, GGAPIGeolocFilter geoloc) {
		
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
		}
		
		if (geoloc != null) {
			Criteria criteria = GGAPIMongoRepository.getCriteriaFromGeolocFilter(geoloc, this.dynamicDomain.geolocalized());
			if( criteria != null)
				query.addCriteria(criteria);
		}
		
		return this.mongo.count(query, this.dtoClass);
	}

	@Override
	public void setDomain(GGAPIDynamicDomain domain) {
		super.setDomain(domain);
		
		String geolocField = domain.geolocalized();
		if( geolocField != null && !geolocField.isEmpty())
			this.mongo.indexOps(this.domainObj.getDtoClass()).ensureIndex( new GeospatialIndex(geolocField).typed(GeoSpatialIndexType.GEO_2DSPHERE) );
	}
	
}
