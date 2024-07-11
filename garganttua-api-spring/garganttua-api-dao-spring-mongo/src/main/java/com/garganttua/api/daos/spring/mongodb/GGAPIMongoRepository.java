package com.garganttua.api.daos.spring.mongodb;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;

import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.sort.IGGAPISort;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		}
		
//		if (geoloc != null) {
//			Criteria criteria = GGAPIMongoRepository.getCriteriaFromGeolocFilter(geoloc, domain.geolocalized);
//			if( criteria != null)
//				query.addCriteria(criteria);
//		}
		
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
			log.debug("		[domain ["+this.domain.getEntity().getValue1().domain()+"]] Finding objects using "+query.toString());
		}
		results = this.mongo.find(query, this.dtoClass);

		return results;
	}

	private static String getTextCriteriaFromFilter(IGGAPIFilter filter) {
		if( filter.getName().equals(GGAPILiteral.OPERATOR_TEXT) ) {
			return (String) filter.getValue();
		} else if( !GGAPILiteral.isFinal(filter) ){
			for(IGGAPIFilter sub: filter.getLiterals() ) {
				String text = getTextCriteriaFromFilter(sub);
				if( text != null && !text.isEmpty() ) {
					return text;
				}
			}
		}
		return null;
	}

//	private static Criteria getCriteriaFromGeolocFilter(GGAPIGeolocFilter geoloc, String geolocField) {
//		Criteria criteria = null;
//		
//		if( geolocField != null ) {
//			Shape shape = geoloc.getShape().accept(new GGAPIGeoJsonToSpringShapeBinder());
//			
//			criteria = Criteria.where(geolocField);
//			switch(geoloc.getType()) {
//			default:
//				break;
//			case GEO_WITHIN:
//				criteria.within(shape);
//				break;
//			case GEO_WITHIN_SPHERE:
//				criteria.withinSphere((Circle) shape);
//				break;
//			}
//		}
//		
//		return criteria;
//	}

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
				criteria = Criteria.where(literal.getValue().equals("uuid")?"_id":literal.getValue().toString()).isNullValue();
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
		}
		
//		if (geoloc != null) {
//			Criteria criteria = GGAPIMongoRepository.getCriteriaFromGeolocFilter(geoloc, domain.geolocalized);
//			if( criteria != null)
//				query.addCriteria(criteria);
//		}
		
		return this.mongo.count(query, this.dtoClass);
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
		this.domain = domain;
		if( domain.getEntity().getValue1().geolocalizedEntity() ) {
			GGObjectAddress geolocField = domain.getEntity().getValue1().locationFieldAddress();
			if( geolocField != null )
				this.mongo.indexOps(this.dtoClass).ensureIndex( new GeospatialIndex(geolocField.getFields()[geolocField.getFields().length-1]).typed(GeoSpatialIndexType.GEO_2DSPHERE) );
		};
	}
	
}